package com.github.pricemonitor.scraper;

import com.github.pricemonitor.config.WebDriverConfig;
import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.model.dto.ScrapedProduct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.Currency;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.pricemonitor.exception.ExceptionCode.E010;
import static com.github.pricemonitor.exception.ExceptionCode.E012;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ShopScraper {

    private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(5);

    private final WebDriverConfig webDriverConfig;

    public ScrapedProduct scrape(final String url) {
        final Document doc = this.getDocument(url);
        final String name = this.extractName(doc);
        final String price = this.extractPrice(doc);
        final URI imageUrl = this.extractImage(doc);
        final Currency currency = this.extractCurrency(doc);

        if (StringUtils.isBlank(name) || StringUtils.isBlank(price)) {
            throw new PmRuntimeException(E010);
        }

        return new ScrapedProduct(name, this.parsePrice(price), imageUrl, currency);
    }

    public abstract boolean supports(final String url);

    protected String extractName(final Document doc) {
        return Optional.ofNullable(doc.selectFirst("meta[property=og:title]"))
                .map(element -> element.attr("content"))
                .or(() -> Optional.ofNullable(doc.selectFirst("title")).map(Element::text))
                .filter(StringUtils::isNotBlank)
                .orElse(null);
    }

    protected String extractPrice(final Document doc) {
        return Stream.of("meta[property=product:price:amount]", "meta[itemprop=price]")
                .map(doc::selectFirst)
                .filter(Objects::nonNull)
                .map(element -> element.attr("content"))
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse(null);
    }

    protected URI extractImage(final Document doc) {
        return Optional.ofNullable(doc.selectFirst("meta[property=og:image]"))
                .map(element -> element.attr("content"))
                .filter(StringUtils::isNotBlank)
                .map(URI::create)
                .orElse(null);
    }

    protected Currency extractCurrency(final Document doc) {
        return Currency.getInstance("PLN");
    }

    private Document getDocument(final String url) {
        WebDriver driver = null;

        try {
            driver = this.webDriverConfig.createDriver();
            driver.get(url);

            final WebDriverWait wait = new WebDriverWait(driver, PAGE_LOAD_TIMEOUT);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            final String pageSource = driver.getPageSource();
            return Jsoup.parse(Objects.requireNonNull(pageSource));

        } catch (final Exception e) {
            throw new PmRuntimeException(E012, e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private BigDecimal parsePrice(final String rawPrice) {
        final String cleanPrice = rawPrice.replaceAll("[^0-9,.]", "").replace(",", ".");
        return new BigDecimal(cleanPrice);
    }

}
