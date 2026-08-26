package com.github.pricemonitor.service

import com.github.pricemonitor.model.dto.ScrapedProduct
import com.github.pricemonitor.scraper.ShopScraper
import com.github.pricemonitor.service.impl.ProductScraperServiceImpl
import spock.lang.Specification
import spock.lang.Subject

class ProductScraperServiceSpec extends Specification {

    def scraper1 = Mock(ShopScraper)
    def scraper2 = Mock(ShopScraper)

    @Subject
    def service = new ProductScraperServiceImpl([this.scraper1, this.scraper2])

    def url = "https://example.com/product"
    def scrapedProduct = new ScrapedProduct("Test Product", new BigDecimal("2000.00"), URI.create("http://image.url"), Currency.getInstance("PLN"))

    def "Should scrape product using the first supported scraper"() {
        given:
            this.scraper1.supports(this.url) >> false
            this.scraper2.supports(this.url) >> true

        when:
            def result = this.service.scrapeProduct(this.url)

        then:
            1 * this.scraper2.scrape(this.url) >> this.scrapedProduct
            0 * this.scraper1.scrape(_)
            result == this.scrapedProduct
    }

}
