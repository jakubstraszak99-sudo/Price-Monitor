package com.github.pricemonitor.scraper;

import com.github.pricemonitor.config.WebDriverConfig;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Order(0)
@Component
public class XKomScraper extends ShopScraper {

    private static final String X_KOM_DOMAIN = "x-kom.pl";
    private static final String SHOP_NAME = "x-kom";
    private static final String TITLE_SELECTOR = "h1";
    private static final String PRODUCT_PRICE_SELECTOR = "div[data-name=productPrice] span";
    private static final String PRODUCT_INFO_SELECTOR = "div[data-name=productInfo], div[data-name=productDetails]";
    private static final String PRICE_TEXT_MATCH = "span:contains(zł)";
    private static final String UNAVAILABLE_SELECTOR = "[data-name=productUnavailable], [data-name=notifyMeButton]";

    public XKomScraper(final WebDriverConfig webDriverConfig) {
        super(webDriverConfig);
    }

    @Override
    public boolean supports(final String url) {
        return url != null && url.contains(X_KOM_DOMAIN);
    }

    @Override
    protected boolean isAvailable(final Document doc) {
        return doc.selectFirst(UNAVAILABLE_SELECTOR) == null && super.isAvailable(doc);
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
        final Element priceElement = doc.selectFirst(PRODUCT_PRICE_SELECTOR);
        if (priceElement != null && StringUtils.isNotBlank(priceElement.text())) {
            return priceElement.text();
        }

        final Element infoPanel = doc.selectFirst(PRODUCT_INFO_SELECTOR);
        if (infoPanel != null) {
            final Element priceSpan = infoPanel.selectFirst(PRICE_TEXT_MATCH);
            if (priceSpan != null && StringUtils.isNotBlank(priceSpan.text())) {
                return priceSpan.text();
            }
        }

        return super.extractPrice(doc);
    }

    @Override
    protected String extractShop(final String url) {
        return SHOP_NAME;
    }

}
