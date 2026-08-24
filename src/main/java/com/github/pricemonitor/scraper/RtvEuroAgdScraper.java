package com.github.pricemonitor.scraper;

import com.github.pricemonitor.config.WebDriverConfig;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Order(3)
@Component
public class RtvEuroAgdScraper extends ShopScraper {

    private static final String EURO_DOMAIN = "euro.com.pl";

    public RtvEuroAgdScraper(final WebDriverConfig webDriverConfig) {
        super(webDriverConfig);
    }

    @Override
    public boolean supports(final String url) {
        return url != null && url.contains(EURO_DOMAIN);
    }

    @Override
    protected String extractName(final Document doc) {
        return Optional.ofNullable(doc.selectFirst("h1.selenium-product-title"))
                .map(Element::text)
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> Optional.ofNullable(doc.selectFirst("h1"))
                        .map(Element::text)
                        .orElseGet(() -> super.extractName(doc)));
    }

    @Override
    protected String extractPrice(final Document doc) {
        final String basePrice = super.extractPrice(doc);
        if (StringUtils.isNotBlank(basePrice)) {
            return basePrice;
        }

        final Element schemaScript = doc.selectFirst("script#product-card-schema");
        if (schemaScript != null) {
            final String json = schemaScript.data();
            final java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"price\":\\s*([0-9.]+)").matcher(json);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return Optional.ofNullable(doc.selectFirst("div.price-normal"))
                .map(Element::text)
                .orElse(super.extractPrice(doc));
    }

}
