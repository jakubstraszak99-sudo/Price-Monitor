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

    public MoreleScraper(final WebDriverConfig webDriverConfig) {
        super(webDriverConfig);
    }

    @Override
    public boolean supports(final String url) {
        return url != null && url.contains(MORELE_DOMAIN);
    }

    @Override
    protected String extractName(final Document doc) {
        return Optional.ofNullable(doc.selectFirst("h1.prod-name"))
                .map(element -> element.attr("data-default"))
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> Optional.ofNullable(doc.selectFirst("h1"))
                        .map(Element::text)
                        .orElseGet(() -> super.extractName(doc)));
    }

    @Override
    protected String extractPrice(final Document doc) {
        return Optional.ofNullable(doc.selectFirst("div#product_price"))
                .map(element -> element.attr("data-price"))
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> super.extractPrice(doc));
    }

}
