package com.github.pricemonitor.scheduler

import com.github.pricemonitor.model.dto.Product
import com.github.pricemonitor.model.page.ProductPage
import com.github.pricemonitor.service.ProductService
import org.springframework.data.domain.PageRequest
import spock.lang.Specification
import spock.lang.Subject

class PriceUpdateSchedulerSpec extends Specification {

    def productService = Mock(ProductService)

    @Subject
    def scheduler = new PriceUpdateScheduler(this.productService)

    def "Should fetch products in batches and trigger price checks"() {
        given:
            def url1 = "https://example.com/product-1"
            def url2 = "https://example.com/product-2"

            def product1 = Mock(Product) { productUrl() >> URI.create(url1) }
            def product2 = Mock(Product) { productUrl() >> URI.create(url2) }

            def pageable = PageRequest.of(0, 100)
            def firstPage = new ProductPage([product1, product2], pageable, 2)

        when:
            this.scheduler.schedulePriceUpdates()

        then:
            1 * this.productService.getProducts(pageable, null) >> firstPage
            1 * this.productService.requestPriceCheck(url1)
            1 * this.productService.requestPriceCheck(url2)
    }

}
