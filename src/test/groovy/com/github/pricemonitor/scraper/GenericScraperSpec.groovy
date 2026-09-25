package com.github.pricemonitor.scraper

import com.github.pricemonitor.config.WebDriverConfig
import org.jsoup.Jsoup
import spock.lang.Specification
import spock.lang.Subject

class GenericScraperSpec extends Specification {

    def webDriverConfig = Mock(WebDriverConfig)

    @Subject
    def scraper = new GenericScraper(this.webDriverConfig)

    def "Should extract currency properly"() {
        given:
            def doc = Jsoup.parse(html)

        when:
            def currency = this.scraper.extractCurrency(doc)

        then:
            currency?.getCurrencyCode() == expectedResult

        where:
            html                                                                                || expectedResult
            "<html><head><meta property='product:price:currency' content='USD'/></head></html>" || "USD"
            "<html><head><meta itemprop='priceCurrency' content='EUR'/></head></html>"          || "EUR"
            "<html><head><meta property='product:price:currency' content=''/></head></html>"    || "PLN"
            "<html><body>Text</body></html>"                                                    || "PLN"
    }

    def "Should derive shop name from domain rather than public suffix"() {
        expect:
            this.scraper.extractShop(url) == expected

        where:
            url                                   || expected
            "https://www.example.co.uk/produkt"   || "Example"
            "https://shop.example.com.au/produkt" || "Example"
            "https://www.EXAMPLE.pl/produkt"      || "Example"
            "https://example.com/produkt"         || "Example"
            "https://example.com./produkt"        || "Example"
            "https://myshop.blogspot.com/produkt" || "Myshop"
            "http://localhost:9090/produkt.html"  || "Localhost"
            "http://127.0.0.1:9090/produkt.html"  || "127.0.0.1"
            "http://[::1]:9090/produkt.html"      || "[::1]"
            "http://shop.internal/produkt.html"   || "Shop.internal"
            "https://com.pl/"                     || "Com.pl"
            "/produkt.html"                       || null
            "invalid url"                         || null
            null                                  || null
    }

}
