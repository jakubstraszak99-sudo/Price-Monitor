package com.github.pricemonitor.scraper;

import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.model.dto.ScrapedProduct;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Currency;

import static com.github.pricemonitor.exception.ExceptionCode.E008;

@Order(0)
@Component
public class XKomScraper extends ShopScraper {

    private static final String X_KOM_DOMAIN = "x-kom.pl";

    @Override
    public boolean supports(final String url) {
        return url != null && url.contains(X_KOM_DOMAIN);
    }

    @Override
    public ScrapedProduct scrape(final String url) throws IOException {
        final Document doc = this.getDocument(url);
        final Element titleElement = doc.selectFirst("h1");
        final Element imageElement = doc.selectFirst("meta[property=og:image]");
        final Element priceElement = doc.selectFirst("div[data-name=productPrice] span");
        final String name = titleElement != null ? titleElement.text() : null;
        final String imageUrl = imageElement != null ? imageElement.attr("content") : null;
        String price = priceElement != null ? priceElement.text() : null;

        if (price == null || price.isBlank()) {
            final Element priceSpan = doc.selectFirst("div[id=app] span:contains(zł)");
            if (priceSpan != null) {
                price = priceSpan.text();
            }
        }

        if (name == null || price == null) {
            throw new PmRuntimeException(E008);
        }

        return new ScrapedProduct(name, this.parsePrice(price), imageUrl, Currency.getInstance("PLN"));
    }

}
