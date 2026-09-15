package com.github.pricemonitor.scraper;

import com.github.pricemonitor.config.WebDriverConfig;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.Map;
import java.util.Optional;

@Order(4)
@Component
public class AmazonScraper extends ShopScraper {

    private static final String AMAZON_DOMAIN = "amazon.";
    private static final String TITLE_SELECTOR = "#productTitle";
    private static final String OFFSCREEN_PRICE_SELECTOR = "span.a-price span.a-offscreen";
    private static final String WHOLE_PRICE_SELECTOR = "span.a-price-whole";
    private static final String FRACTION_PRICE_SELECTOR = "span.a-price-fraction";
    private static final String LEGACY_PRICE_SELECTOR = "#priceblock_ourprice, #priceblock_dealprice, #priceblock_saleprice";
    private static final String LANDING_IMAGE_SELECTOR = "#landingImage";
    private static final String DATA_OLD_HIRES_ATTR = "data-old-hires";
    private static final String SRC_ATTR = "src";

    public AmazonScraper(final WebDriverConfig webDriverConfig) {
        super(webDriverConfig);
    }

    @Override
    public boolean supports(final String url) {
        return url != null && url.contains(AMAZON_DOMAIN);
    }

    @Override
    protected String extractName(final Document doc) {
        return Optional.ofNullable(doc.selectFirst(TITLE_SELECTOR))
                .map(Element::text)
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> super.extractName(doc));
    }

    @Override
    protected String extractPrice(final Document doc) {
        return this.findPrice(doc)
                .map(this::normalizePrice)
                .orElseGet(() -> super.extractPrice(doc));
    }

    @Override
    protected String extractImage(final Document doc) {
        return Optional.ofNullable(doc.selectFirst(LANDING_IMAGE_SELECTOR))
                .map(element -> {
                    final String hiRes = element.attr(DATA_OLD_HIRES_ATTR);
                    return StringUtils.isNotBlank(hiRes) ? hiRes : element.attr(SRC_ATTR);
                })
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> super.extractImage(doc));
    }

    @Override
    protected Currency extractCurrency(final Document doc) {
        return this.findPrice(doc)
                .map(this::parseCurrency)
                .orElseGet(() -> super.extractCurrency(doc));
    }

    private Optional<String> findPrice(final Document doc) {
        return this.findOffscreenPrice(doc).or(() -> this.findWholePlusFraction(doc))
                .or(() -> Optional.ofNullable(doc.selectFirst(LEGACY_PRICE_SELECTOR))
                        .map(Element::text)
                        .filter(StringUtils::isNotBlank));
    }

    private Optional<String> findOffscreenPrice(final Document doc) {
        return Optional.ofNullable(doc.selectFirst(OFFSCREEN_PRICE_SELECTOR))
                .map(Element::text)
                .filter(StringUtils::isNotBlank);
    }

    private Optional<String> findWholePlusFraction(final Document doc) {
        return Optional.ofNullable(doc.selectFirst(WHOLE_PRICE_SELECTOR))
                .map(Element::text)
                .filter(StringUtils::isNotBlank)
                .map(whole -> {
                    final String fraction = Optional.ofNullable(doc.selectFirst(FRACTION_PRICE_SELECTOR))
                            .map(Element::text)
                            .filter(StringUtils::isNotBlank)
                            .orElse(null);
                    return fraction != null ? whole + "." + fraction : whole;
                });
    }

    private String normalizePrice(final String rawPrice) {
        if (rawPrice.contains(".") && rawPrice.contains(",")) {
            return rawPrice.replace(",", "");
        }

        return rawPrice;
    }

    @Nullable
    private Currency parseCurrency(final String text) {
        for (final Map.Entry<String, String> entry : CURRENCY_SYMBOLS.entrySet()) {
            if (text.contains(entry.getKey())) {
                return Currency.getInstance(entry.getValue());
            }
        }

        return null;
    }

}
