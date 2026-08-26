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

    public GunfireScraper(final WebDriverConfig webDriverConfig) {
        super(webDriverConfig);
    }

    @Override
    public boolean supports(final String url) {
        return url != null && url.contains(GUNFIRE_DOMAIN);
    }

    @Override
    protected String extractPrice(Document doc) {
        final String price = Optional.ofNullable(doc.selectFirst("strong.projector_price_value"))
                .map(element -> {
                    final String dataPrice = element.attr("data-price");
                    return StringUtils.isNotBlank(dataPrice) ? dataPrice : element.text();
                })
                .filter(StringUtils::isNotBlank)
                .map(raw -> raw.replaceAll("[^0-9.,]", "").replace(",", "."))
                .orElse(null);

        if (price != null) {
            return price;
        }

        return Optional.ofNullable(doc.selectFirst("span.projector_price_srp"))
                .map(Element::text)
                .filter(StringUtils::isNotBlank)
                .map(raw -> raw.replaceAll("[^0-9.,]", "").replace(",", "."))
                .orElseGet(() -> super.extractPrice(doc));
    }

}
