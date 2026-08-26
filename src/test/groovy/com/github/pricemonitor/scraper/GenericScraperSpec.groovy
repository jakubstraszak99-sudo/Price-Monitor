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

}
