package com.github.pricemonitor.scraper;

import com.github.pricemonitor.config.WebDriverConfig;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.Objects;
import java.util.stream.Stream;

@Order(Integer.MAX_VALUE)
@Component
public class GenericScraper extends ShopScraper {

    private static final String CONTENT_ATTR = "content";
    private static final String PRICE_CURRENCY_LABEL_1 = "meta[property=product:price:currency]";
    private static final String PRICE_CURRENCY_LABEL_2 = "meta[itemprop=priceCurrency]";

    public GenericScraper(final WebDriverConfig webDriverConfig) {
        super(webDriverConfig);
    }

    @Override
    public boolean supports(final String url) {
        return true;
    }

    @Override
    protected Currency extractCurrency(final Document doc) {
        return Stream.of(PRICE_CURRENCY_LABEL_1, PRICE_CURRENCY_LABEL_2)
                .map(doc::selectFirst)
                .filter(Objects::nonNull)
                .map(element -> element.attr(CONTENT_ATTR))
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .map(Currency::getInstance)
                .orElse(super.extractCurrency(doc));
    }

}
