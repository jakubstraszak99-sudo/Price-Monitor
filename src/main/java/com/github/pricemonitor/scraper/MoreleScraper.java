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

@Order(1)
@Component
public class MoreleScraper extends ShopScraper {

    private static final String MORELE_DOMAIN = "morele.net";

    @Override
    public boolean supports(final String url) {
        return url != null && url.contains(MORELE_DOMAIN);
    }

    @Override
    public ScrapedProduct scrape(final String url) throws IOException {
        final Document doc = this.getDocument(url);
        final Element titleElement = doc.selectFirst("h1.prod-name");
        final Element imageElement = doc.selectFirst("meta[property=og:image]");
        final Element priceElement = doc.selectFirst("div#product_price");
        final String name = titleElement != null ? titleElement.attr("data-default") :
                (doc.selectFirst("h1") != null ? Objects.requireNonNull(doc.selectFirst("h1")).text() : null);
        final String imageUrl = imageElement != null ? imageElement.attr("content") : null;
        String price = priceElement != null ? priceElement.attr("data-price") : null;

        if (price == null || price.isBlank()) {
            final Element fallbackPrice = doc.selectFirst("meta[itemprop=price]");
            if (fallbackPrice != null) {
                price = fallbackPrice.attr("content");
            }
        }

        if (name == null || price == null) {
            throw new PmRuntimeException(E008);
        }

        return new ScrapedProduct(name, this.parsePrice(price), imageUrl, Currency.getInstance("PLN"));
    }

}
