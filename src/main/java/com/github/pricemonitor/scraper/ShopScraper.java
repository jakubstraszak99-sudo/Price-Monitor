package com.github.pricemonitor.scraper;

import com.github.pricemonitor.model.dto.ScrapedProduct;
import jakarta.annotation.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.math.BigDecimal;

public abstract class ShopScraper {

    @Value("${app.agent}")
    private String userAgent;

    public abstract boolean supports(final String url);

    public abstract ScrapedProduct scrape(final String url) throws IOException;

    protected Document getDocument(final String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(this.userAgent)
                .timeout(5000)
                .get();
    }

    protected BigDecimal parsePrice(final String rawPrice) {
        final String cleanPrice = rawPrice.replaceAll("[^0-9,.]", "").replace(",", ".");
        return new BigDecimal(cleanPrice);
    }

}
