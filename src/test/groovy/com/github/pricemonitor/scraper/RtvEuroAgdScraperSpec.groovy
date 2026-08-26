package com.github.pricemonitor.scraper

import com.github.pricemonitor.config.WebDriverConfig
import org.jsoup.Jsoup
import spock.lang.Specification
import spock.lang.Subject

class RtvEuroAgdScraperSpec extends Specification {

    def webDriverConfig = Mock(WebDriverConfig)

    @Subject
    def scraper = new RtvEuroAgdScraper(this.webDriverConfig)

    def "Should extract name properly"() {
        given:
            def doc = Jsoup.parse(html)

        when:
            def name = this.scraper.extractName(doc)

        then:
            name == expectedResult

        where:
            html                                                                           || expectedResult
            "<html><body><h1 class='selenium-product-title'>Dishwasher</h1></body></html>" || "Dishwasher"
            "<html><body><h1>Simple H1</h1></body></html>"                                 || "Simple H1"
            "<html><head><title>Base class text</title></head></html>"                     || "Base class text"
            "<html><body>Text</body></html>"                                               || null
    }

    def "Should extract price properly"() {
        given:
            def doc = Jsoup.parse(html)

        when:
            def price = this.scraper.extractPrice(doc)

        then:
            price == expectedResult

        where:
            html                                                                                                               || expectedResult
            "<html><head><meta property='product:price:amount' content='1999.00'/></head></html>"                              || "1999.00"
            "<html><body><script id='product-card-schema'>{\"@type\": \"Product\", \"price\": 2499.99}</script></body></html>" || "2499.99"
            "<html><body><script id='product-card-schema'>{\"price\":3000}</script></body></html>"                             || "3000"
            "<html><body><div class='price-normal'>1 299 zł</div></body></html>"                                               || "1 299 zł"
            "<html><body>Text</body></html>"                                                                                   || null
    }

}
