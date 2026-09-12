package com.github.pricemonitor.scraper;

import com.github.pricemonitor.config.WebDriverConfig;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Order(3)
@Component
public class RtvEuroAgdScraper extends ShopScraper {

    private static final String EURO_DOMAIN = "euro.com.pl";
    private static final String SHOP_NAME = "RTV EURO AGD";
    private static final String TITLE_SELECTOR = "h1.selenium-product-title";
    private static final String FALLBACK_TITLE_SELECTOR = "h1";
    private static final String SCHEMA_SCRIPT_SELECTOR = "script#product-card-schema";
    private static final String PRICE_NORMAL_SELECTOR = "div.price-normal";
    private static final Pattern SCHEMA_PRICE_PATTERN = Pattern.compile("\"price\":\\s*([0-9.]+)");

    public RtvEuroAgdScraper(final WebDriverConfig webDriverConfig) {
        super(webDriverConfig);
    }

    @Override
    public boolean supports(final String url) {
        return url != null && url.contains(EURO_DOMAIN);
    }

    @Override
    protected String extractName(final Document doc) {
        return Optional.ofNullable(doc.selectFirst(TITLE_SELECTOR))
                .map(Element::text)
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> Optional.ofNullable(doc.selectFirst(FALLBACK_TITLE_SELECTOR))
                        .map(Element::text)
                        .orElseGet(() -> super.extractName(doc)));
    }

    @Override
    protected String extractPrice(final Document doc) {
        final String basePrice = super.extractPrice(doc);
        if (StringUtils.isNotBlank(basePrice)) {
            return basePrice;
        }

        final Element schemaScript = doc.selectFirst(SCHEMA_SCRIPT_SELECTOR);
        if (schemaScript != null) {
            final Matcher matcher = SCHEMA_PRICE_PATTERN.matcher(schemaScript.data());
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return Optional.ofNullable(doc.selectFirst(PRICE_NORMAL_SELECTOR))
                .map(Element::text)
                .orElse(super.extractPrice(doc));
    }

    @Override
    protected String extractShop(final String url) {
        return SHOP_NAME;
    }

}
