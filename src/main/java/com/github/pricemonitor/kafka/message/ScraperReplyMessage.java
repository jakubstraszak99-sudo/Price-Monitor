package com.github.pricemonitor.kafka.message;

import com.github.pricemonitor.exception.ExceptionCode;
import com.github.pricemonitor.model.dto.ScrapedProduct;

public record ScraperReplyMessage(
        String url,
        ScrapedProduct scrapedProduct,
        boolean success,
        String error,
        ExceptionCode errorCode
) implements KafkaMessage {

    public static ScraperReplyMessage success(final String url, final ScrapedProduct product) {
        return new ScraperReplyMessage(url, product, true, null, null);
    }

    public static ScraperReplyMessage failure(final String url, final String error, final ExceptionCode errorCode) {
        return new ScraperReplyMessage(url, null, false, error, errorCode);
    }

}
