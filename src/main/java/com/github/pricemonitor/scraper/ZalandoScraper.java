package com.github.pricemonitor.scraper;

import com.github.pricemonitor.config.WebDriverConfig;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Order(8)
@Component
public class ZalandoScraper extends ShopScraper {

    private static final String ZALANDO_DOMAIN = "zalando.";
    private static final String PRODUCT_NAME_SELECTOR = "h1";
    private static final String PRODUCT_PRICE_SELECTOR = "div[data-testid='pdp-price-container'] p span";

    public ZalandoScraper(final WebDriverConfig webDriverConfig) {
        super(webDriverConfig);
    }

    @Override
    public boolean supports(final String url) {
        return url != null && url.contains(ZALANDO_DOMAIN);
    }

    @Override
    protected String extractName(final Document doc) {
        return Optional.ofNullable(doc.selectFirst(PRODUCT_NAME_SELECTOR))
                .map(Element::text)
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> super.extractName(doc));
    }

    @Override
    protected String extractPrice(final Document doc) {
        return Optional.ofNullable(doc.selectFirst(PRODUCT_PRICE_SELECTOR))
                .map(Element::text)
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> super.extractPrice(doc));
    }

}
