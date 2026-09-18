package com.github.pricemonitor.scraper;

import com.github.pricemonitor.config.WebDriverConfig;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Order(7)
@Component
public class SteamScraper extends ShopScraper {

    private static final String STEAM_DOMAIN = "steampowered.com/app";
    private static final String SHOP_NAME = "Steam";
    private static final String APP_NAME_SELECTOR = "div#appHubAppName";
    private static final String DISCOUNT_PRICE_SELECTOR = "div.discount_final_price";
    private static final String PURCHASE_PRICE_SELECTOR = "div.game_purchase_price";
    private static final String PURCHASE_GAME_SELECTOR = "div.game_area_purchase_game";
    private static final String FREE_TO_PLAY_LABEL = "Free to Play";
    private static final String FREE_LABEL = "Free";
    private static final String FREE_PRICE_VALUE = "0.00";

    public SteamScraper(final WebDriverConfig webDriverConfig) {
        super(webDriverConfig);
    }

    @Override
    public boolean supports(final String url) {
        return url != null && url.contains(STEAM_DOMAIN);
    }

    @Override
    protected String extractName(final Document doc) {
        return Optional.ofNullable(doc.selectFirst(APP_NAME_SELECTOR))
                .map(Element::text)
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> super.extractName(doc));
    }

    @Override
    protected String extractPrice(final Document doc) {
        if (this.isFreeToPlay(doc)) {
            return FREE_PRICE_VALUE;
        }
        return this.findPrice(doc)
                .map(rawPrice -> Strings.CI.contains(rawPrice, FREE_LABEL) ? FREE_PRICE_VALUE : rawPrice)
                .orElseGet(() -> super.extractPrice(doc));
    }

    @Override
    protected Currency extractCurrency(final Document doc) {
        return this.findPrice(doc)
                .map(this::parseCurrency)
                .orElseGet(() -> super.extractCurrency(doc));
    }

    @Override
    protected String extractShop(final String url) {
        return SHOP_NAME;
    }

    private boolean isFreeToPlay(final Document doc) {
        final Element purchaseBlock = doc.selectFirst(PURCHASE_GAME_SELECTOR);
        return purchaseBlock != null && Strings.CI.contains(purchaseBlock.text(), FREE_TO_PLAY_LABEL);
    }

    private Optional<String> findPrice(final Document doc) {
        final Element primaryPurchaseBlock = doc.selectFirst(PURCHASE_GAME_SELECTOR);

        if (primaryPurchaseBlock == null) {
            return Optional.empty();
        }

        return Stream.of(DISCOUNT_PRICE_SELECTOR, PURCHASE_PRICE_SELECTOR)
                .map(primaryPurchaseBlock::selectFirst)
                .filter(Objects::nonNull)
                .map(Element::text)
                .filter(StringUtils::isNotBlank)
                .findFirst();
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
