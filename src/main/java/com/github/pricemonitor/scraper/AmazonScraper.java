package com.github.pricemonitor.scraper;

import com.github.pricemonitor.config.WebDriverConfig;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Order(4)
@Component
public class AmazonScraper extends ShopScraper {

    private static final String AMAZON_DOMAIN = "amazon.";
    private static final String TITLE_SELECTOR = "#productTitle";
    private static final String BUY_BOX_CONTAINER_SELECTOR = "#corePriceDisplay_desktop_feature_div, #corePrice_feature_div, #corePrice_desktop, #tp_price_block_total_price_ww, #apex_desktop, #unifiedPrice_feature_div";
    private static final String OUT_OF_STOCK_SELECTOR = "#outOfStock";
    private static final String AVAILABILITY_SELECTOR = "#availability";
    private static final String OFFSCREEN_PRICE_SELECTOR = "span.a-price span.a-offscreen";
    private static final String WHOLE_PRICE_SELECTOR = "span.a-price-whole";
    private static final String FRACTION_PRICE_SELECTOR = "span.a-price-fraction";
    private static final String LEGACY_PRICE_SELECTOR = "#priceblock_ourprice, #priceblock_dealprice, #priceblock_saleprice";
    private static final String LANDING_IMAGE_SELECTOR = "#landingImage";
    private static final String DATA_OLD_HIRES_ATTR = "data-old-hires";
    private static final String SRC_ATTR = "src";
    private static final List<String> UNAVAILABLE_PHRASES = List.of(
            "niedostępny",
            "unavailable",
            "out of stock",
            "brak"
    );

    public AmazonScraper(final WebDriverConfig webDriverConfig) {
        super(webDriverConfig);
    }

    @Override
    public boolean supports(final String url) {
        return url != null && url.contains(AMAZON_DOMAIN);
    }

    @Override
    protected boolean isAvailable(final Document doc) {
        if (doc.selectFirst(OUT_OF_STOCK_SELECTOR) != null) {
            return false;
        }

        final Element availability = doc.selectFirst(AVAILABILITY_SELECTOR);

        if (availability != null) {
            final String availabilityText = availability.text().toLowerCase();
            final boolean isUnavailable = UNAVAILABLE_PHRASES.stream()
                    .anyMatch(availabilityText::contains);

            return !isUnavailable;
        }

        return true;
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
        final Element buyBox = doc.selectFirst(BUY_BOX_CONTAINER_SELECTOR);
        final Element searchRoot = buyBox != null ? buyBox : doc;
        return this.findOffscreenPrice(searchRoot).or(() -> this.findWholePlusFraction(searchRoot))
                .or(() -> Optional.ofNullable(doc.selectFirst(LEGACY_PRICE_SELECTOR))
                        .map(Element::text)
                        .filter(StringUtils::isNotBlank));
    }


    private Optional<String> findOffscreenPrice(final Element root) {
        return Optional.ofNullable(root.selectFirst(OFFSCREEN_PRICE_SELECTOR))
                .map(Element::text)
                .filter(StringUtils::isNotBlank);
    }

    private Optional<String> findWholePlusFraction(final Element root) {
        return Optional.ofNullable(root.selectFirst(WHOLE_PRICE_SELECTOR))
                .map(Element::text)
                .filter(StringUtils::isNotBlank)
                .map(whole -> {
                    final String fraction = Optional.ofNullable(root.selectFirst(FRACTION_PRICE_SELECTOR))
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
