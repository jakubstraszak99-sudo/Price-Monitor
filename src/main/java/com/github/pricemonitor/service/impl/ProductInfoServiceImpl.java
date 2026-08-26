package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.kafka.KafkaEventPublisher;
import com.github.pricemonitor.kafka.message.ScraperReplyMessage;
import com.github.pricemonitor.kafka.message.ScraperRequestMessage;
import com.github.pricemonitor.model.dto.ScrapedProduct;
import com.github.pricemonitor.model.mapper.ProductMapper;
import com.github.pricemonitor.service.ProductInfoService;
import com.github.pricemonitor.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.github.pricemonitor.exception.ExceptionCode.E011;
import static com.github.pricemonitor.utils.KafkaUtil.SCRAPER_REQUEST_TOPIC;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductInfoServiceImpl implements ProductInfoService {

    private final ProductService productService;
    private final ProductMapper mapper;
    private final KafkaEventPublisher kafkaEventPublisher;

    @Override
    public ScrapedProduct getProductInfo(final String url) {
        return this.productService.findProduct(url)
                .map(product -> {
                    log.info("Product found in database: {}", url);
                    return this.mapper.mapToScrapedProduct(product);
                }).orElseGet(() -> this.fetchFromUrl(url));
    }

    private ScrapedProduct fetchFromUrl(final String url) {
        final ScraperRequestMessage request = new ScraperRequestMessage(url);
        final ScraperReplyMessage reply = this.kafkaEventPublisher.publishAndReceive(SCRAPER_REQUEST_TOPIC, url, request);

        if (!reply.success() || reply.scrapedProduct() == null) {
            throw new PmRuntimeException(E011);
        }

        return reply.scrapedProduct();
    }

}
