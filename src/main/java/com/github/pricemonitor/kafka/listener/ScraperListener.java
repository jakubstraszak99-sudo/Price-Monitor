package com.github.pricemonitor.kafka.listener;

import com.github.pricemonitor.kafka.message.ScraperReplyMessage;
import com.github.pricemonitor.kafka.message.ScraperRequestMessage;
import com.github.pricemonitor.model.dto.ScrapedProduct;
import com.github.pricemonitor.service.ProductScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import static com.github.pricemonitor.utils.KafkaUtil.SCRAPER_REQUEST_TOPIC;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScraperListener {

    private final ProductScraperService productScraperService;

    @KafkaListener(topics = SCRAPER_REQUEST_TOPIC, containerFactory = "scraperRequestContainerFactory")
    @SendTo
    public ScraperReplyMessage handleScrapingRequest(final ScraperRequestMessage message) {
        log.info("Received scraping request event for url: {}", message.url());

        try {
            final ScrapedProduct product = this.productScraperService.scrapeProduct(message.url());
            return ScraperReplyMessage.success(product);
        } catch (final Exception e) {
            log.error("An error occurred during scraping process: {}", e.getMessage());
            return ScraperReplyMessage.failure(e.getMessage());
        }
    }

}
