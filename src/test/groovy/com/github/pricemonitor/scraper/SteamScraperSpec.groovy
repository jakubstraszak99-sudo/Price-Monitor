package com.github.pricemonitor.scraper

import com.github.pricemonitor.config.WebDriverConfig
import org.jsoup.Jsoup
import spock.lang.Specification
import spock.lang.Subject

class SteamScraperSpec extends Specification {

    def webDriverConfig = Mock(WebDriverConfig)

    @Subject
    def scraper = new SteamScraper(this.webDriverConfig)

    def "Should extract name properly"() {
        given:
            def doc = Jsoup.parse(html)

        when:
            def name = this.scraper.extractName(doc)

        then:
            name == expectedResult

        where:
            html                                                            || expectedResult
            "<html><body><div id='appHubAppName'>CS:GO</div></body></html>" || "CS:GO"
            "<html><head><title>Basic title</title></head></html>"          || "Basic title"
            "<html><body>No title</body></html>"                            || null
    }

    def "Should extract price properly"() {
        given:
            def doc = Jsoup.parse(html)

        when:
            def price = this.scraper.extractPrice(doc)

        then:
            price == expectedResult

        where:
            html                                                                                         || expectedResult
            "<html><body><div class='game_area_purchase_game'>Play for Free to Play</div></body></html>" || "0.00"
            "<html><body><div class='discount_final_price'>99,99 zł</div></body></html>"                 || "99,99 zł"
            "<html><body><div class='game_purchase_price'>49,99€</div></body></html>"                    || "49,99€"
            "<html><body><div class='game_purchase_price'>Free</div></body></html>"                      || "0.00"
            "<html><head><meta property='product:price:amount' content='19.99'/></head></html>"          || "19.99"
            "<html><body>No price</body></html>"                                                         || null
    }

    def "Should extract currency properly"() {
        given:
            def doc = Jsoup.parse(html)

        when:
            def currency = this.scraper.extractCurrency(doc)

        then:
            currency?.getCurrencyCode() == expectedResult

        where:
            html                                                                         || expectedResult
            "<html><body><div class='discount_final_price'>99,99 zł</div></body></html>" || "PLN"
            "<html><body><div class='game_purchase_price'>49,99€</div></body></html>"    || "EUR"
            "<html><body><div class='game_purchase_price'>19.99\$</div></body></html>"   || "USD"
            "<html><body><div class='game_purchase_price'>10.00£</div></body></html>"    || "GBP"
            "<html><body><div class='game_purchase_price'>10.00 CHF</div></body></html>" || "CHF"
            "<html><body><div class='game_purchase_price'>100 SEK</div></body></html>"   || "SEK"
            "<html><body>1 NSC (Not supported currency)</body></html>"                   || "PLN"
    }

}
