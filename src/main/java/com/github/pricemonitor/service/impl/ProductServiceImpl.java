package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.kafka.KafkaEventPublisher;
import com.github.pricemonitor.kafka.message.ScraperReplyMessage;
import com.github.pricemonitor.kafka.message.ScraperRequestMessage;
import com.github.pricemonitor.model.dto.Product;
import com.github.pricemonitor.model.dto.ScrapedProduct;
import com.github.pricemonitor.model.entity.ProductEntity;
import com.github.pricemonitor.model.mapper.ProductMapper;
import com.github.pricemonitor.model.page.ProductPage;
import com.github.pricemonitor.repository.ProductRepository;
import com.github.pricemonitor.service.PriceAlertNotificationService;
import com.github.pricemonitor.service.PriceHistoryService;
import com.github.pricemonitor.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import static com.github.pricemonitor.exception.ExceptionCode.E011;
import static com.github.pricemonitor.kafka.KafkaConstants.SCRAPER_REPLY_TOPIC;
import static com.github.pricemonitor.kafka.KafkaConstants.SCRAPER_REQUEST_TOPIC;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final KafkaEventPublisher eventPublisher;
    private final PriceHistoryService priceHistoryService;
    private final PriceAlertNotificationService priceAlertNotificationService;

    @Override
    @Transactional
    public ProductEntity getOrCreateProduct(final String url, final ScrapedProduct data) {
        return this.findProduct(url).orElseGet(() -> this.saveNewProduct(url, data));
    }

    @Override
    public ScrapedProduct getProductData(final String url) {
        return this.findProduct(url)
                .map(product -> {
                    log.debug("Product found in database: {}", url);
                    return this.productMapper.mapToScrapedProduct(product);
                }).orElseGet(() -> this.fetchFromUrl(url));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPage getProducts(final Pageable pageable, final String search) {
        final Pageable pageableWithAvailabilityFirst = this.prioritizeAvailable(pageable);
        final Page<ProductEntity> products = (search != null && !search.isBlank())
                ? this.productRepository.searchByNameOrShop(search, pageableWithAvailabilityFirst)
                : this.productRepository.findAll(pageableWithAvailabilityFirst);
        final Page<Product> page = products.map(this.productMapper::map);
        return new ProductPage(page.getContent(), pageable, page.getTotalElements());
    }


    @Override
    @Transactional
    public void updateProduct(final String productUrl, final ScrapedProduct scrapedProduct) {
        this.findProduct(productUrl).ifPresent(product -> this.updateProductData(product, scrapedProduct));
    }

    @Override
    @Transactional
    public void markUnavailable(String productUrl) {
        this.findProduct(productUrl).ifPresent(product -> {
            product.setAvailable(false);
            product.getPriceAlerts().clear();
        });
    }

    @Override
    public void requestProductCheck(final String url) {
        final ScraperRequestMessage message = new ScraperRequestMessage(url);
        this.eventPublisher.publish(SCRAPER_REQUEST_TOPIC, url, message, SCRAPER_REPLY_TOPIC);
    }

    private Optional<ProductEntity> findProduct(final String url) {
        return this.productRepository.findByProductUrl(url);
    }

    private Pageable prioritizeAvailable(final Pageable pageable) {
        final Sort availableFirst = Sort.by(Sort.Order.desc("available")).and(pageable.getSort());
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), availableFirst);
    }

    private ScrapedProduct fetchFromUrl(final String url) {
        final ScraperRequestMessage request = new ScraperRequestMessage(url);
        final ScraperReplyMessage reply = this.eventPublisher.publishAndReceive(SCRAPER_REQUEST_TOPIC, url, request);

        if (!reply.success() || reply.scrapedProduct() == null) {
            throw new PmRuntimeException(E011);
        }

        return reply.scrapedProduct();
    }

    private ProductEntity saveNewProduct(final String url, final ScrapedProduct data) {
        final ProductEntity product = this.productRepository.save(this.productMapper.map(data, url));
        this.priceHistoryService.createPriceHistory(product);

        return product;
    }

    private void updateProductData(final ProductEntity product, final ScrapedProduct data) {
        this.updatePrice(product, data.price());
        this.updateName(product, data.name());
        this.updateImage(product, data.imageUrl());
        this.updateFavicon(product, data.faviconUrl());
        product.setAvailable(true);
    }

    private void updatePrice(final ProductEntity product, final BigDecimal newPrice) {
        if (newPrice != null && newPrice.compareTo(product.getCurrentPrice()) != 0) {
            product.setCurrentPrice(newPrice);
            this.priceHistoryService.createPriceHistory(product, newPrice);
            this.priceAlertNotificationService.notifyAboutPriceChange(product, newPrice);
        }
    }

    private void updateName(final ProductEntity product, final String newName) {
        if (newName != null && !product.getName().equals(newName)) {
            product.setName(newName);
        }
    }

    private void updateImage(final ProductEntity product, final URI newImage) {
        if (newImage != null && !Objects.equals(product.getImageUrl(), newImage.toString())) {
            product.setImageUrl(newImage.toString());
        }
    }

    private void updateFavicon(final ProductEntity product, final URI newFavicon) {
        if (newFavicon != null && !Objects.equals(product.getFaviconUrl(), newFavicon.toString())) {
            product.setFaviconUrl(newFavicon.toString());
        }
    }

}