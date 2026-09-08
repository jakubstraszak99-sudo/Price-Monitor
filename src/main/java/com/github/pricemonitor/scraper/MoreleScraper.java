package com.github.pricemonitor.scraper;

import com.github.pricemonitor.config.WebDriverConfig;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Order(1)
@Component
public class MoreleScraper extends ShopScraper {

    private static final String MORELE_DOMAIN = "morele.net";
    private static final String PRODUCT_NAME_SELECTOR = "h1.prod-name";
    private static final String FALLBACK_TITLE_SELECTOR = "h1";
    private static final String PRODUCT_PRICE_SELECTOR = "div#product_price";
    private static final String DATA_DEFAULT_ATTR = "data-default";
    private static final String DATA_PRICE_ATTR = "data-price";

    public MoreleScraper(final WebDriverConfig webDriverConfig) {
        super(webDriverConfig);
    }

    @Override
    public boolean supports(final String url) {
        return url != null && url.contains(MORELE_DOMAIN);
    }

    @Override
    protected String extractName(final Document doc) {
        return Optional.ofNullable(doc.selectFirst(PRODUCT_NAME_SELECTOR))
                .map(element -> element.attr(DATA_DEFAULT_ATTR))
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> Optional.ofNullable(doc.selectFirst(FALLBACK_TITLE_SELECTOR))
                        .map(Element::text)
                        .orElseGet(() -> super.extractName(doc)));
    }

    @Override
    protected String extractPrice(final Document doc) {
        return Optional.ofNullable(doc.selectFirst(PRODUCT_PRICE_SELECTOR))
                .map(element -> element.attr(DATA_PRICE_ATTR))
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> super.extractPrice(doc));
    }

}