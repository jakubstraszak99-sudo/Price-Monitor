package com.github.pricemonitor.scraper

import com.github.pricemonitor.config.WebDriverConfig
import org.jsoup.Jsoup
import spock.lang.Specification
import spock.lang.Subject

class EmpikScraperSpec extends Specification {

    def webDriverConfig = Mock(WebDriverConfig)

    @Subject
    def scraper = new EmpikScraper(this.webDriverConfig)

    def "Should extract product name using dedicated selector"() {
        given:
            def html = """
                    <html>
                        <body>
                            <h1 data-ta="title">LEGO test</h1>
                        </body>
                    </html>
                """
            def doc = Jsoup.parse(html)

        expect:
            this.scraper.extractName(doc) == "LEGO test"
    }

    def "Should extract product price using dedicated selector"() {
        given:
            def html = """
                        <html>
                            <body>
                                <span data-ta="price">1389,99 zł</span>
                                <div data-ta-section="priceMainContainer">
                                    <span data-ta="price">1242,99 zł</span>
                                </div>
                            </body>
                        </html>
                    """
            def doc = Jsoup.parse(html)

        expect:
            this.scraper.extractPrice(doc) == "1242,99 zł"
    }

}
