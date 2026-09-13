package com.github.pricemonitor.kafka.message;

import com.github.pricemonitor.model.dto.ScrapedProduct;

public record ScraperReplyMessage(
        String url,
        ScrapedProduct scrapedProduct,
        boolean success,
        String error
) implements KafkaMessage {

    public static ScraperReplyMessage success(final String url, final ScrapedProduct product) {
        return new ScraperReplyMessage(url, product, true, null);
    }

    public static ScraperReplyMessage failure(final String url, final String error) {
        return new ScraperReplyMessage(url, null, false, error);
    }

}
