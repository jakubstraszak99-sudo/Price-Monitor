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

    public GenericScraper(final WebDriverConfig webDriverConfig) {
        super(webDriverConfig);
    }

    @Override
    public boolean supports(final String url) {
        return true;
    }

    @Override
    protected Currency extractCurrency(final Document doc) {
        return Stream.of("meta[property=product:price:currency]", "meta[itemprop=priceCurrency]")
                .map(doc::selectFirst)
                .filter(Objects::nonNull)
                .map(element -> element.attr("content"))
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .map(Currency::getInstance)
                .orElse(super.extractCurrency(doc));
    }

}
