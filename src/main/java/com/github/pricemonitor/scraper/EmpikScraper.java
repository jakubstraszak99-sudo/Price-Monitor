package com.github.pricemonitor.scraper;

import com.github.pricemonitor.config.WebDriverConfig;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Order(7)
@Component
public class EmpikScraper extends ShopScraper {

    private static final String EMPIK_DOMAIN = "empik.com";
    private static final String PRODUCT_NAME_SELECTOR = "h1[data-ta='title']";
    private static final String PRODUCT_PRICE_SELECTOR = "div[data-ta-section='priceMainContainer'] span[data-ta='price']";

    public EmpikScraper(final WebDriverConfig webDriverConfig) {
        super(webDriverConfig);
    }

    @Override
    public boolean supports(final String url) {
        return url != null && url.contains(EMPIK_DOMAIN);
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
