package com.github.pricemonitor.scraper

import com.github.pricemonitor.config.WebDriverConfig
import org.jsoup.Jsoup
import spock.lang.Specification
import spock.lang.Subject

class GunfireScraperSpec extends Specification {

    def webDriverConfig = Mock(WebDriverConfig)

    @Subject
    def scraper = new GunfireScraper(this.webDriverConfig)

    def "Should extract price properly"() {
        given:
            def doc = Jsoup.parse(html)

        when:
            def price = this.scraper.extractPrice(doc)

        then:
            price == expectedResult

        where:
            html                                                                                                          || expectedResult
            "<html><body><strong class='projector_price_value' data-price='1,234.56'>1000</strong></body></html>"         || "1,234.56"
            "<html><body><strong class='projector_price_value'>999,99 zł</strong></body></html>"                          || "999,99 zł"
            "<html><body><span class='projector_price_srp'>888,88 PLN</span></body></html>"                               || "888,88 PLN"
            "<html><head><meta property='product:price:amount' content='777.77'/></head></html>"                          || "777.77"
            "<html><body>Text</body></html>"                                                                              || null
    }

}
