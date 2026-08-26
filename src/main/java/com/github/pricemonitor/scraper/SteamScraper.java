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

@Order(4)
@Component
public class SteamScraper extends ShopScraper {

    private static final String STEAM_DOMAIN = "steampowered.com/app";

    private static final Map<String, String> CURRENCY_SYMBOLS = Map.of(
            "zł", "PLN",
            "€", "EUR",
            "$", "USD",
            "£", "GBP",
            "CHF", "CHF",
            "SEK", "SEK"
    );

    public SteamScraper(final WebDriverConfig webDriverConfig) {
        super(webDriverConfig);
    }

    @Override
    public boolean supports(final String url) {
        return url != null && url.contains(STEAM_DOMAIN);
    }

    @Override
    protected String extractName(final Document doc) {
        return Optional.ofNullable(doc.selectFirst("div#appHubAppName"))
                .map(Element::text)
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> super.extractName(doc));
    }

    @Override
    protected String extractPrice(final Document doc) {
        if (this.isFreeToPlay(doc)) {
            return "0.00";
        }

        return this.findPrice(doc)
                .map(rawPrice -> Strings.CI.contains(rawPrice, "Free") ? "0.00" : rawPrice)
                .orElseGet(() -> super.extractPrice(doc));
    }

    @Override
    protected Currency extractCurrency(final Document doc) {
        return this.findPrice(doc)
                .map(this::parseCurrency)
                .orElseGet(() -> super.extractCurrency(doc));
    }

    private boolean isFreeToPlay(final Document doc) {
        final Element purchaseBlock = doc.selectFirst("div.game_area_purchase_game");
        return purchaseBlock != null && Strings.CI.contains(purchaseBlock.text(), "Free to Play");
    }

    private Optional<String> findPrice(final Document doc) {
        return Stream.of("div.discount_final_price", "div.game_purchase_price")
                .map(doc::selectFirst)
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
