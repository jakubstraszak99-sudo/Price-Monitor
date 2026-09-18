package com.github.pricemonitor.scraper;

import com.github.pricemonitor.config.WebDriverConfig;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Order(2)
@Component
public class MediaExpertScraper extends ShopScraper {

    private static final String MEDIA_EXPERT_DOMAIN = "mediaexpert.pl";
    private static final String SHOP_NAME = "Media Expert";
    private static final String TITLE_SELECTOR = "h1.is-title";
    private static final String FALLBACK_TITLE_SELECTOR = "h1";
    private static final String MAIN_PRICE_WHOLE_SELECTOR = ".product-main-section .main-price span.whole";
    private static final String MAIN_PRICE_CENTS_SELECTOR = ".product-main-section .main-price span.cents";

    public MediaExpertScraper(final WebDriverConfig webDriverConfig) {
        super(webDriverConfig);
    }

    @Override
    public boolean supports(final String url) {
        return url != null && url.contains(MEDIA_EXPERT_DOMAIN);
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
        Element wholeElement = doc.selectFirst(MAIN_PRICE_WHOLE_SELECTOR);
        Element centsElement = doc.selectFirst(MAIN_PRICE_CENTS_SELECTOR);

        if (wholeElement != null) {
            String price = wholeElement.text().replaceAll("\\D", "");

            if (centsElement != null) {
                price += "." + centsElement.text().replaceAll("\\D", "");
            }

            return price;
        }

        return super.extractPrice(doc);
    }

    @Override
    protected String extractShop(final String url) {
        return SHOP_NAME;
    }

}