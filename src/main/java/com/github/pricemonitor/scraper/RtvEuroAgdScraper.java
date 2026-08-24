package com.github.pricemonitor.scraper;

import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.model.dto.ScrapedProduct;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Currency;
import java.util.Objects;

import static com.github.pricemonitor.exception.ExceptionCode.E008;

@Order(2)
@Component
public class RtvEuroAgdScraper extends ShopScraper {

    private static final String EURO_DOMAIN = "euro.com.pl";

    @Override
    public boolean supports(final String url) {
        return url != null && url.contains(EURO_DOMAIN);
    }

    @Override
    public ScrapedProduct scrape(final String url) throws IOException {
        final Document doc = this.getDocument(url);
        final Element titleElement = doc.selectFirst("h1.selenium-product-title");
        final Element imageElement = doc.selectFirst("meta[property=og:image]");
        final Element priceElement = doc.selectFirst("meta[property=product:price:amount]");
        final String name = titleElement != null ? titleElement.text() :
                (doc.selectFirst("h1") != null ? Objects.requireNonNull(doc.selectFirst("h1")).text() : null);
        final String imageUrl = imageElement != null ? imageElement.attr("content") : null;
        String price = priceElement != null ? priceElement.attr("content") : null;

        if (price == null || price.isBlank()) {
            final Element priceDiv = doc.selectFirst("div.price-normal");
            if (priceDiv != null) {
                price = priceDiv.text();
            }
        }

        if (name == null || price == null) {
            throw new PmRuntimeException(E008);
        }

        return new ScrapedProduct(name, this.parsePrice(price), imageUrl, Currency.getInstance("PLN"));
    }

}
