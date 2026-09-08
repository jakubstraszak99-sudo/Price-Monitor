package com.github.pricemonitor.scraper;

import com.github.pricemonitor.config.WebDriverConfig;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Order(5)
@Component
public class GunfireScraper extends ShopScraper {

    private static final String GUNFIRE_DOMAIN = "gunfire.com";
    private static final String PRICE_VALUE_SELECTOR = "strong.projector_price_value";
    private static final String PRICE_SRP_SELECTOR = "span.projector_price_srp";
    private static final String DATA_PRICE_ATTR = "data-price";

    public GunfireScraper(final WebDriverConfig webDriverConfig) {
        super(webDriverConfig);
    }

    @Override
    public boolean supports(final String url) {
        return url != null && url.contains(GUNFIRE_DOMAIN);
    }

    @Override
    protected String extractPrice(final Document doc) {
        final String price = Optional.ofNullable(doc.selectFirst(PRICE_VALUE_SELECTOR))
                .map(element -> {
                    final String dataPrice = element.attr(DATA_PRICE_ATTR);
                    return StringUtils.isNotBlank(dataPrice) ? dataPrice : element.text();
                })
                .filter(StringUtils::isNotBlank)
                .orElse(null);

        if (price != null) {
            return price;
        }

        return Optional.ofNullable(doc.selectFirst(PRICE_SRP_SELECTOR))
                .map(Element::text)
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> super.extractPrice(doc));
    }

}
