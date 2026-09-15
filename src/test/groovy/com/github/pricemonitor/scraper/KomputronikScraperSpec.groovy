package com.github.pricemonitor.scraper

import com.github.pricemonitor.config.WebDriverConfig
import org.jsoup.Jsoup
import spock.lang.Specification
import spock.lang.Subject

class KomputronikScraperSpec extends Specification {

    def webDriverConfig = Mock(WebDriverConfig)

    @Subject
    def scraper = new KomputronikScraper(webDriverConfig)

    def "Should extract product name properly"() {
        given:
            def html = """
                <html>
                    <body>
                        <h1 data-name="productName">Test Pro Max 5G</h1>
                    </body>
                </html>
            """
            def doc = Jsoup.parse(html)

        when:
            def name = scraper.extractName(doc)

        then:
            name == "Test Pro Max 5G"
    }

    def "Should extract price from data-price-type attribute as text"() {
        given:
            def html = """
                <html>
                    <body>
                        <div data-price-type="final">2 399 zł</div>
                    </body>
                </html>
            """
            def doc = Jsoup.parse(html)

        when:
            def price = scraper.extractPrice(doc)

        then:
            price == "2 399 zł"
    }

    def "Should extract price from meta tag content attribute as fallback"() {
        given: "HTML document with Open Graph meta tags"
            def html = """
                <html>
                    <head>
                        <meta property="product:price:amount" content="2399.00" />
                    </head>
                    <body>
                        <!-- No visible price tag to simulate fallback -->
                    </body>
                </html>
            """
            def doc = Jsoup.parse(html)

        when:
            def price = scraper.extractPrice(doc)

        then:
            price == "2399.00"
    }

}
