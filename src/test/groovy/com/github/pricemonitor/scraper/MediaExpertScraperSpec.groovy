package com.github.pricemonitor.scraper

import com.github.pricemonitor.config.WebDriverConfig
import org.jsoup.Jsoup
import spock.lang.Specification
import spock.lang.Subject

class MediaExpertScraperSpec extends Specification {

    def webDriverConfig = Mock(WebDriverConfig)

    @Subject
    def scraper = new MediaExpertScraper(this.webDriverConfig)

    def "Should extract name properly"() {
        given:
            def doc = Jsoup.parse(html)

        when:
            def name = this.scraper.extractName(doc)

        then:
            name == expectedResult

        where:
            html                                                       || expectedResult
            "<html><body><h1 class='is-title'>TV</h1></body></html>"   || "TV"
            "<html><body><h1>Simple H1</h1></body></html>"             || "Simple H1"
            "<html><head><title>Base class text</title></head></html>" || "Base class text"
            "<html><body>Text</body></html>"                           || null
    }

    def "Should extract price properly"() {
        given:
            def doc = Jsoup.parse(html)

        when:
            def price = this.scraper.extractPrice(doc)

        then:
            price == expectedResult

        where:
            html                                                                                     || expectedResult
            "<html><head><meta property='product:price:amount' content='3499.00'/></head></html>"    || "3499.00"
            "<html><body><span class='whole'>4999</span><span class='cents'>99</span></body></html>" || "4999.99"
            "<html><body><span class='whole'>5000</span></body></html>"                              || "5000"
            "<html><body>Text</body></html>"                                                         || null
    }

}
