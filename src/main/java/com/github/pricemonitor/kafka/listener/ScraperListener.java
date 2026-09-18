package com.github.pricemonitor.kafka.listener;

import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.kafka.message.ScraperReplyMessage;
import com.github.pricemonitor.kafka.message.ScraperRequestMessage;
import com.github.pricemonitor.model.dto.ScrapedProduct;
import com.github.pricemonitor.service.ProductService;
import com.github.pricemonitor.service.ScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import static com.github.pricemonitor.exception.ExceptionCode.E016;
import static com.github.pricemonitor.kafka.KafkaConstants.SCRAPER_REPLY_TOPIC;
import static com.github.pricemonitor.kafka.KafkaConstants.SCRAPER_REQUEST_TOPIC;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScraperListener {

    private final ScraperService scraperService;
    private final ProductService productService;

    @KafkaListener(topics = SCRAPER_REQUEST_TOPIC, containerFactory = "scraperRequestContainerFactory")
    @SendTo
    public ScraperReplyMessage handleScrapingRequest(final ScraperRequestMessage message) {
        log.debug("Received scraping request event for url: {}", message.url());

        try {
            final ScrapedProduct product = this.scraperService.scrapeProduct(message.url());
            return ScraperReplyMessage.success(message.url(), product);
        } catch (final Exception e) {
            log.error("An error occurred during scraping process: {}", e.getMessage());
            return this.handleScrapingException(message.url(), e);
        }

    }

    @KafkaListener(topics = SCRAPER_REPLY_TOPIC, containerFactory = "scraperReplyContainerFactory")
    public void handleScrapingReply(final ScraperReplyMessage message) {
        log.debug("Received scraping reply event for url: {}", message.url());

        if (!message.success() || message.scrapedProduct() == null) {
            log.error("Scraping failed: {}", message.error());

            if (message.errorCode() == E016) {
                this.productService.markUnavailable(message.url());
            }
        }

        this.productService.updateProduct(message.url(), message.scrapedProduct());
    }

    private ScraperReplyMessage handleScrapingException(final String url, final Exception exception) {
        log.error("An error occurred during scraping process for url: {}", url, exception);

        if (exception instanceof PmRuntimeException e) {
            return ScraperReplyMessage.failure(url, e.getMessage(), e.getCode());
        }

        return ScraperReplyMessage.failure(url, exception.getMessage(), null);
    }

}
