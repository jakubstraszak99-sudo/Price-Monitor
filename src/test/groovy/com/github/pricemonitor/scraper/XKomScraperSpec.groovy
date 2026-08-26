package com.github.pricemonitor.scraper

import com.github.pricemonitor.config.WebDriverConfig
import org.jsoup.Jsoup
import spock.lang.Specification
import spock.lang.Subject

class XKomScraperSpec extends Specification{

    def webDriverConfig = Mock(WebDriverConfig)

    @Subject
    def scraper = new XKomScraper(this.webDriverConfig)

    def "Should extract name properly"() {
        given:
            def doc = Jsoup.parse(html)

        when:
            def name = this.scraper.extractName(doc)

        then:
            name == expectedResult

        where:
            html                                                             || expectedResult
            "<html><body><h1>Phone</h1></body></html>"                       || "Phone"
            "<html><head><title>Notebook</title></head><body></body></html>" || "Notebook"
            "<html><body>Text</body></html>"                                 || null
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
            "<html><body><div data-name='productPrice'><span>4 999,00 zł</span></div></body></html>" || "4 999,00 zł"
            "<html><body><div id='app'><span>3 499,00 zł</span></div></body></html>"                 || "3 499,00 zł"
            "<html><head><meta property='product:price:amount' content='2999.99'/></head></html>"    || "2999.99"
            "<html><body>Text</body></html>"                                                         || null
    }

}
