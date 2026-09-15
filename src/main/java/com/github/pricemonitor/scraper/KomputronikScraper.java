package com.github.pricemonitor.scraper;

import com.github.pricemonitor.config.WebDriverConfig;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Order(6)
@Component
public class KomputronikScraper extends ShopScraper {

    private static final String KOMPUTRONIK_DOMAIN = "komputronik.pl";
    private static final String CONTENT_ATTR = "content";
    private static final String TITLE_SELECTOR = "h1[data-name='productName']";
    private static final String PRICE_SELECTOR = "[data-price-type='final'], meta[property='product:price:amount']";

    public KomputronikScraper(final WebDriverConfig webDriverConfig) {
        super(webDriverConfig);
    }

    @Override
    public boolean supports(final String url) {
        return url != null && url.contains(KOMPUTRONIK_DOMAIN);
    }

    @Override
    protected String extractName(final Document doc) {
        return Optional.ofNullable(doc.selectFirst(TITLE_SELECTOR))
                .map(Element::text)
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> super.extractName(doc));
    }

    @Override
    protected String extractPrice(final Document doc) {
        final Element priceElement = doc.selectFirst(PRICE_SELECTOR);

        if (priceElement != null) {
            final String price = priceElement.hasAttr(CONTENT_ATTR)
                    ? priceElement.attr(CONTENT_ATTR)
                    : priceElement.text();

            if (StringUtils.isNotBlank(price)) {
                return price;
            }
        }

        return super.extractPrice(doc);
    }

}
