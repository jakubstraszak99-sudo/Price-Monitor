package com.github.pricemonitor.scraper;

import com.github.pricemonitor.config.WebDriverConfig;
import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.model.dto.ScrapedProduct;
import jakarta.annotation.Nullable;
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
    private static final String FAVICON_SELECTOR = "link[rel~=(?i)^(shortcut icon|icon|apple-touch-icon|apple-touch-icon-precomposed)$]";
    private static final String OG_TITLE_SELECTOR = "meta[property=og:title]";
    private static final String TITLE_SELECTOR = "title";
    private static final String OG_IMAGE_SELECTOR = "meta[property=og:image]";
    private static final String PRICE_AMOUNT_SELECTOR = "meta[property=product:price:amount]";
    private static final String PRICE_ITEMPROP_SELECTOR = "meta[itemprop=price]";
    private static final String CONTENT_ATTR = "content";
    private static final String ABS_HREF_ATTR = "abs:href";
    private static final String DEFAULT_CURRENCY_CODE = "PLN";
    private static final String GOOGLE_FAVICON_URL_TEMPLATE = "https://www.google.com/s2/favicons?domain=%s&sz=64";

    private final WebDriverConfig webDriverConfig;

    public ScrapedProduct scrape(final String url) {
        final Document doc = this.getDocument(url);
        final String name = this.extractName(doc);
        final String price = this.extractPrice(doc);
        final String imageUrl = this.extractImage(doc);
        final Currency currency = this.extractCurrency(doc);
        final String domain = this.extractDomain(url);
        final String faviconUrl = this.extractFavicon(doc, url);

        if (StringUtils.isBlank(name) || StringUtils.isBlank(price)) {
            throw new PmRuntimeException(E010);
        }

        return new ScrapedProduct(name, this.parsePrice(price), URI.create(imageUrl), currency, domain, URI.create(faviconUrl));
    }

    public abstract boolean supports(final String url);

    protected String extractName(final Document doc) {
        return Optional.ofNullable(doc.selectFirst(OG_TITLE_SELECTOR))
                .map(element -> element.attr(CONTENT_ATTR))
                .or(() -> Optional.ofNullable(doc.selectFirst(TITLE_SELECTOR)).map(Element::text))
                .filter(StringUtils::isNotBlank)
                .orElse(null);
    }

    protected String extractPrice(final Document doc) {
        return Stream.of(PRICE_AMOUNT_SELECTOR, PRICE_ITEMPROP_SELECTOR)
                .map(doc::selectFirst)
                .filter(Objects::nonNull)
                .map(element -> element.attr(CONTENT_ATTR))
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse(null);
    }

    protected String extractImage(final Document doc) {
        return Optional.ofNullable(doc.selectFirst(OG_IMAGE_SELECTOR))
                .map(element -> element.attr(CONTENT_ATTR))
                .filter(StringUtils::isNotBlank)
                .orElse(null);
    }

    protected Currency extractCurrency(final Document doc) {
        return Currency.getInstance(DEFAULT_CURRENCY_CODE);
    }

    protected String extractDomain(final String url) {
        return this.capitalize(this.extractDomainLabel(url));
    }

    protected String extractFavicon(final Document doc, final String url) {
        return Optional.ofNullable(doc.selectFirst(FAVICON_SELECTOR))
                .map(element -> element.attr(ABS_HREF_ATTR))
                .filter(StringUtils::isNotBlank)
                .orElseGet(() -> this.fallbackFaviconUrl(url));
    }

    private Document getDocument(final String url) {
        WebDriver driver = null;

        try {
            driver = this.webDriverConfig.createDriver();
            driver.get(url);

            final WebDriverWait wait = new WebDriverWait(driver, PAGE_LOAD_TIMEOUT);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            final String pageSource = driver.getPageSource();
            return Jsoup.parse(Objects.requireNonNull(pageSource), url);
        } catch (final Exception e) {
            throw new PmRuntimeException(E012, e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private String fallbackFaviconUrl(final String url) {
        try {
            final String host = URI.create(url).getHost();
            return String.format(GOOGLE_FAVICON_URL_TEMPLATE, host);
        } catch (final Exception e) {
            return null;
        }
    }

    private String capitalize(final String input) {
        if (StringUtils.isBlank(input)) {
            return input;
        }

        return Character.toUpperCase(input.charAt(0)) + input.substring(1).toLowerCase();
    }

    @Nullable
    private String extractDomainLabel(final String url) {
        try {
            final String host = URI.create(url).getHost();
            if (host == null) {
                return null;
            }
            final String cleanHost = host.startsWith("www.") ? host.substring(4) : host;
            final String[] parts = cleanHost.split("\\.");
            return parts.length >= 2 ? parts[parts.length - 2] : cleanHost;
        } catch (final Exception e) {
            return null;
        }
    }

    private BigDecimal parsePrice(final String rawPrice) {
        final String cleanPrice = rawPrice.replaceAll("[^0-9,.]", "").replace(",", ".");
        return new BigDecimal(cleanPrice);
    }

}
