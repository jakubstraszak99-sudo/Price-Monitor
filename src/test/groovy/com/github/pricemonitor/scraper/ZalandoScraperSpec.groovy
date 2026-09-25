package com.github.pricemonitor.scraper

import com.github.pricemonitor.config.WebDriverConfig
import org.jsoup.Jsoup
import spock.lang.Specification
import spock.lang.Subject

class ZalandoScraperSpec extends Specification {

    def webDriverConfig = Mock(WebDriverConfig)

    @Subject
    def scraper = new ZalandoScraper(this.webDriverConfig)

    def "Should extract product name using dedicated selector"() {
        given:
            def html = """
                    <html>
                        <body>
                            <h1>PUMA Mayze - Sneakersy niskie</h1>
                        </body>
                    </html>
                """
            def doc = Jsoup.parse(html)

        expect:
            this.scraper.extractName(doc) == "PUMA Mayze - Sneakersy niskie"
    }

    def "Should extract product price using dedicated selector"() {
        given:
            def html = """
                    <html>
                        <body>
                            <div data-testid="pdp-price-container">
                                <p>
                                    <span>304,00 zł</span>
                                </p>
                            </div>
                        </body>
                    </html>
                """
            def doc = Jsoup.parse(html)

        expect:
            this.scraper.extractPrice(doc) == "304,00 zł"
    }

}
