package com.github.pricemonitor.scraper

import com.github.pricemonitor.config.WebDriverConfig
import org.jsoup.Jsoup
import spock.lang.Specification
import spock.lang.Subject

class AmazonScraperSpec extends Specification {

    def webDriverConfig = Mock(WebDriverConfig)

    @Subject
    def scraper = new AmazonScraper(this.webDriverConfig)

    def "Should extract name properly"() {
        given:
            def html = """
                <html>
                    <body>
                        <span id="productTitle">
                            Test
                        </span>
                    </body>
                </html>
            """
            def doc = Jsoup.parse(html)

        when:
            def name = scraper.extractName(doc)

        then:
            name == "Test"
    }

    def "Should extract and normalize price from different selectors"() {
        given:
            def doc = Jsoup.parse(html)

        when:
            def price = scraper.extractPrice(doc)

        then:
            price == expectedPrice

        where:
            scenario                       | html                                                                                         || expectedPrice
            "Offscreen price"              | '<html><span class="a-price"><span class="a-offscreen">123,45 zł</span></span></html>'       || "123,45 zł"
            "Offscreen with normalization" | '<html><span class="a-price"><span class="a-offscreen">$1,234.56</span></span></html>'       || "\$1234.56"
            "Whole + fraction price"       | '<html><span class="a-price-whole">99</span><span class="a-price-fraction">90</span></html>' || "99.90"
            "Legacy ourprice"              | '<html><span id="priceblock_ourprice">199.99£</span></html>'                                 || "199.99£"
            "Legacy dealprice"             | '<html><span id="priceblock_dealprice">149.99</span></html>'                                 || "149.99"
            "Legacy saleprice"             | '<html><span id="priceblock_saleprice">99.99€</span></html>'                                 || "99.99€"
    }

    def "Should extract image from data-old-hires attribute if available"() {
        given:
            def html = """
                <html>
                    <body>
                        <img id="landingImage" 
                             data-old-hires="https://amazon.com/high-res-image.jpg" 
                             src="https://amazon.com/low-res-image.jpg" />
                    </body>
                </html>
            """
            def doc = Jsoup.parse(html)

        when:
            def image = scraper.extractImage(doc)

        then:
            image == "https://amazon.com/high-res-image.jpg"
    }

    def "Should ignore decoy price outside the buy-box container (regression for unavailable-product bug)"() {
        given:
            def html = """
                <html>
                    <body>
                        <div id="corePriceDisplay_desktop_feature_div">
                            <span class="a-price"><span class="a-offscreen">299,99 zł</span></span>
                        </div>
                        <div id="sims-consolidated-1_feature_div">
                            <span class="a-price"><span class="a-offscreen">89,00 zł</span></span>
                        </div>
                    </body>
                </html>
            """
            def doc = Jsoup.parse(html)

        when:
            def price = scraper.extractPrice(doc)

        then:
            price == "299,99 zł"
    }

    def "Should fall back to page-wide search when no known buy-box container is present"() {
        given:
            def html = '<html><span class="a-price"><span class="a-offscreen">123,45 zł</span></span></html>'
            def doc = Jsoup.parse(html)

        when:
            def price = scraper.extractPrice(doc)

        then:
            price == "123,45 zł"
    }

    def "Should treat product as unavailable when #outOfStock is present"() {
        given:
            def doc = Jsoup.parse("<html><body><div id='outOfStock'>Currently unavailable</div></body></html>")

        expect:
            !scraper.isAvailable(doc)
    }

    def "Should treat product as unavailable when availability block has no buy button"() {
        given:
            def doc = Jsoup.parse("<html><body><div id='availability'><span>Chwilowo niedostępny</span></div></body></html>")

        expect:
            !scraper.isAvailable(doc)
    }

    def "Should treat product as available when a buy button is present alongside availability info"() {
        given:
            def doc = Jsoup.parse("""
                <html><body>
                    <div id='availability'><span>In stock</span></div>
                    <button id='add-to-cart-button'>Add to cart</button>
                </body></html>
            """)

        expect:
            scraper.isAvailable(doc)
    }

}