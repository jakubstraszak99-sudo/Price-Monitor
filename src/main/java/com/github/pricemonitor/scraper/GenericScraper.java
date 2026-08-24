package com.github.pricemonitor.scraper;

import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.model.dto.ScrapedProduct;
import jakarta.annotation.Nullable;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Currency;

import static com.github.pricemonitor.exception.ExceptionCode.E008;

@Order(Integer.MAX_VALUE)
@Component
public class GenericScraper extends ShopScraper {

    @Override
    public boolean supports(final String url) {
        return true;
    }

    @Override
    public ScrapedProduct scrape(final String url) throws IOException {
        final Document doc = this.getDocument(url);
        final String name = this.extractName(doc);
        final String imageUrl = this.extractImage(doc);
        final String price = this.extractPrice(doc);
        final Currency currency = Currency.getInstance(this.extractCurrency(doc));

        if (price == null || name == null) {
            throw new PmRuntimeException(E008);
        }

        return new ScrapedProduct(name, this.parsePrice(price), imageUrl, currency);
    }

    @Nullable
    private String extractName(final Document doc) {
        final Element ogTitle = doc.selectFirst("meta[property=og:title]");

        if (ogTitle != null) {
            return ogTitle.attr("content");
        }

        final Element title = doc.selectFirst("title");

        if (title != null) {
            return title.text();
        }

        return null;
    }

    @Nullable
    private String extractImage(final Document doc) {
        final Element ogImage = doc.selectFirst("meta[property=og:image]");

        if (ogImage != null) {
            return ogImage.attr("content");
        }

        return null;
    }

    @Nullable
    private String extractPrice(final Document doc) {
        final Element productPrice = doc.selectFirst("meta[property=product:price:amount]");

        if (productPrice != null) {
            return productPrice.attr("content");
        }

        final Element itemPropPrice = doc.selectFirst("meta[itemprop=price]");

        if (itemPropPrice != null) {
            return itemPropPrice.attr("content");
        }

        return null;
    }

    private String extractCurrency(final Document doc) {
        final Element productCurrency = doc.selectFirst("meta[property=product:price:currency]");

        if (productCurrency != null) {
            return productCurrency.attr("content");
        }

        final Element itemPropCurrency = doc.selectFirst("meta[itemprop=priceCurrency]");

        if (itemPropCurrency != null) {
            return itemPropCurrency.attr("content");
        }

        return "PLN";
    }

}
