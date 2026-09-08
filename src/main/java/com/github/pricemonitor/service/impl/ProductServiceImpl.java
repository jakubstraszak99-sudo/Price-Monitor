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
import com.github.pricemonitor.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.github.pricemonitor.exception.ExceptionCode.E011;
import static com.github.pricemonitor.utils.KafkaUtil.SCRAPER_REQUEST_TOPIC;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final KafkaEventPublisher kafkaEventPublisher;

    @Override
    @Transactional
    public ProductEntity getOrCreateProduct(final String url, final ScrapedProduct data) {
        return this.findProduct(url).orElseGet(() -> this.saveNewProduct(url, data));
    }

    @Override
    public ScrapedProduct getProductInfo(final String url) {
        return this.findProduct(url)
                .map(product -> {
                    log.info("Product found in database: {}", url);
                    return this.productMapper.mapToScrapedProduct(product);
                }).orElseGet(() -> this.fetchFromUrl(url));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPage getProducts(final Pageable pageable, final String search) {
        final Page<ProductEntity> products = (search != null && !search.isBlank())
                ? this.productRepository.findByNameContainingIgnoreCase(search, pageable)
                : this.productRepository.findAll(pageable);
        final Page<Product> page = products.map(this.productMapper::map);
        return new ProductPage(page.getContent(), pageable, page.getTotalElements());
    }

    private Optional<ProductEntity> findProduct(final String url) {
        return this.productRepository.findByProductUrl(url);
    }

    private ScrapedProduct fetchFromUrl(final String url) {
        final ScraperRequestMessage request = new ScraperRequestMessage(url);
        final ScraperReplyMessage reply = this.kafkaEventPublisher.publishAndReceive(SCRAPER_REQUEST_TOPIC, url, request);

        if (!reply.success() || reply.scrapedProduct() == null) {
            throw new PmRuntimeException(E011);
        }

        return reply.scrapedProduct();
    }

    private ProductEntity saveNewProduct(final String url, final ScrapedProduct data) {
        final ProductEntity product = this.productMapper.map(data, url);
        return this.productRepository.save(product);
    }

}
