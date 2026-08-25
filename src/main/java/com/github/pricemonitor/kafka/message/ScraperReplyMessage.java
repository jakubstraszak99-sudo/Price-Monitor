package com.github.pricemonitor.kafka.message;

import com.github.pricemonitor.model.dto.ScrapedProduct;

public record ScraperReplyMessage(
        ScrapedProduct scrapedProduct,
        boolean success,
        String error
) implements KafkaMessage {

    public static ScraperReplyMessage success(final ScrapedProduct product) {
        return new ScraperReplyMessage(product, true, null);
    }

    public static ScraperReplyMessage failure(final String error) {
        return new ScraperReplyMessage(null, false, error);
    }

}
