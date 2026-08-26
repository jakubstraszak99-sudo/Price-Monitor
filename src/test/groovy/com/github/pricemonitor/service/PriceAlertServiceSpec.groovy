package com.github.pricemonitor.service

import com.github.pricemonitor.model.dto.ScrapedProduct
import com.github.pricemonitor.model.entity.ProductEntity
import com.github.pricemonitor.model.entity.UserEntity
import com.github.pricemonitor.model.mapper.PriceAlertMapperImpl
import com.github.pricemonitor.repository.PriceAlertRepository
import com.github.pricemonitor.service.impl.PriceAlertServiceImpl
import spock.lang.Specification
import spock.lang.Subject

class PriceAlertServiceSpec extends Specification {

    def priceAlertRepository = Mock(PriceAlertRepository)
    def productService = Mock(ProductService)
    def userService = Mock(UserService)
    def priceAlertMapper = new PriceAlertMapperImpl()

    @Subject
    def service = new PriceAlertServiceImpl(
            this.priceAlertRepository,
            this.productService,
            this.userService,
            this.priceAlertMapper
    )

    def userPublicId = UUID.randomUUID()
    def url = "https://example.com/product"
    def targetPrice = new BigDecimal("1500.00")
    def scrapedProduct = new ScrapedProduct("Test Product", new BigDecimal("2000.00"), URI.create("http://image.url"), Currency.getInstance("PLN"))

    def "Should successfully create and save price alert"() {
        given:
            def user = new UserEntity(publicId: this.userPublicId, username: "testuser")
            def product = new ProductEntity(productUrl: this.url, name: "Test Product")

            this.userService.getUser(this.userPublicId) >> user
            this.productService.getOrCreateProduct(this.url, this.scrapedProduct) >> product

        when:
            this.service.createPriceAlert(this.url, this.targetPrice, this.scrapedProduct, this.userPublicId)

        then:
            1 * this.priceAlertRepository.save({ alert ->
                alert.user == user &&
                        alert.product == product &&
                        alert.targetPrice == this.targetPrice &&
                        alert.active == true
            })
    }

}
