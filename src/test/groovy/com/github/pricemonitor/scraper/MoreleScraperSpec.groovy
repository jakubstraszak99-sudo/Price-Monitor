package com.github.pricemonitor.scraper

import com.github.pricemonitor.config.WebDriverConfig
import org.jsoup.Jsoup
import spock.lang.Specification
import spock.lang.Subject

class MoreleScraperSpec extends Specification {

    def webDriverConfig = Mock(WebDriverConfig)

    @Subject
    def scraper = new MoreleScraper(this.webDriverConfig)

    def "Should extract name properly"() {
        given:
            def doc = Jsoup.parse(html)

        when:
            def name = this.scraper.extractName(doc)

        then:
            name == expectedResult

        where:
            html                                                                                        || expectedResult
            "<html><body><h1 class='prod-name' data-default='Processor'>sample text</h1></body></html>" || "Processor"
            "<html><body><h1>GPU</h1></body></html>"                                                    || "GPU"
            "<html><head><title>Motherboard</title></head><body></body></html>"                         || "Motherboard"
            "<html><body>Text</body></html>"                                                            || null
    }

    def "Should extract price properly"() {
        given:
            def doc = Jsoup.parse(html)

        when:
            def price = this.scraper.extractPrice(doc)

        then:
            price == expectedResult

        where:
            html                                                                                       || expectedResult
            "<html><body><div id='product_price' data-price='1599.99'>1 599,99 zł</div></body></html>" || "1599.99"
            "<html><head><meta property='product:price:amount' content='1499.00'/></head></html>"      || "1499.00"
            "<html><body>Text</body></html>"                                                           || null
    }

}
