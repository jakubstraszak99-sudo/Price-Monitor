package com.github.pricemonitor.service

import com.github.pricemonitor.exception.ExceptionCode
import com.github.pricemonitor.exception.PmRuntimeException
import com.github.pricemonitor.model.dto.ScrapedProduct
import com.github.pricemonitor.model.entity.PriceAlertEntity
import com.github.pricemonitor.model.entity.ProductEntity
import com.github.pricemonitor.model.entity.UserEntity
import com.github.pricemonitor.model.mapper.PriceAlertMapperImpl
import com.github.pricemonitor.model.mapper.ProductMapperImpl
import com.github.pricemonitor.model.page.PriceAlertPage
import com.github.pricemonitor.repository.PriceAlertRepository
import com.github.pricemonitor.service.impl.PriceAlertServiceImpl
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification
import spock.lang.Subject

class PriceAlertServiceSpec extends Specification {

    def priceAlertRepository = Mock(PriceAlertRepository)
    def productService = Mock(ProductService)
    def userService = Mock(UserService)
    def productMapper = new ProductMapperImpl()
    def priceAlertMapper = new PriceAlertMapperImpl(this.productMapper)

    @Subject
    def service = new PriceAlertServiceImpl(
            this.priceAlertRepository,
            this.productService,
            this.userService,
            this.priceAlertMapper
    )

    def userPublicId = UUID.randomUUID()
    def testUser = new UserEntity(publicId: this.userPublicId, username: "testuser")

    def setup() {
        this.userService.getUserEntity(this.userPublicId) >> this.testUser
    }

    def "Should successfully create and save price alert"() {
        given:
            def url = "https://example.com/product"
            def targetPrice = new BigDecimal("1500.00")
            def scrapedProduct = new ScrapedProduct(
                    "Test Product",
                    new BigDecimal("2000.00"),
                    URI.create("http://image.url"),
                    Currency.getInstance("PLN"),
                    "Test Shop",
                    URI.create("http://favicon.url"))
            def user = new UserEntity(publicId: this.userPublicId, username: "testuser")
            def product = new ProductEntity(productUrl: url, name: "Test Product")

            this.productService.getOrCreateProduct(url, scrapedProduct) >> product

        when:
            this.service.createPriceAlert(url, targetPrice, scrapedProduct, this.userPublicId)

        then:
            1 * this.priceAlertRepository.save({ alert ->
                alert.user == user &&
                    alert.product == product &&
                    alert.targetPrice == targetPrice &&
                    alert.active == true
            })
    }

    def "Should throw E014 when price alert already exists"() {
        given:
            def url = "https://example.com/product"
            def targetPrice = new BigDecimal("1500.00")
            def scrapedProduct = new ScrapedProduct(
                    "Test Product",
                    new BigDecimal("2000.00"),
                    URI.create("http://image.url"),
                    Currency.getInstance("PLN"),
                    "Test Shop",
                    URI.create("http://favicon.url"))
            def product = new ProductEntity(productUrl: url, name: "Test Product")

            this.productService.getOrCreateProduct(url, scrapedProduct) >> product
            this.priceAlertRepository.existsByUserAndProduct(this.testUser, product) >> true

        when:
            this.service.createPriceAlert(url, targetPrice, scrapedProduct, this.userPublicId)

        then:
            def e = thrown(PmRuntimeException)
            e.getCode() == ExceptionCode.E014
            0 * this.priceAlertRepository.save(_)
    }

    def "Should return paginated price alerts"() {
        given:
            def user = new UserEntity(publicId: this.userPublicId, username: "testuser")
            def pageable = PageRequest.of(0, 20)
            def alertEntity = new PriceAlertEntity()
            def entitiesPage = new PageImpl<>([alertEntity], pageable, 1)

        when:
            def result = this.service.getPriceAlerts(pageable, null, this.userPublicId)

        then:
            1 * priceAlertRepository.findByUser(user, pageable) >> entitiesPage
            0 * priceAlertRepository.findByUserAndProductNameContainingIgnoreCase(_, _, _)

            result instanceof PriceAlertPage
            result.content.size() == 1
            result.totalElements == 1
    }

    def "Should return filtered price alerts when search term is provided"() {
        given:
            def user = new UserEntity(publicId: this.userPublicId, username: "testuser")
            def searchTerm = "Test"
            def product = ProductEntity.builder()
                    .id(1L)
                    .name("Test")
                    .build()
            def pageable = PageRequest.of(0, 20)
            def alertEntity = new PriceAlertEntity()
            alertEntity.setProduct(product)
            def filteredPage = new PageImpl<PriceAlertEntity>([alertEntity], pageable, 1)
            this.priceAlertRepository.findByUserAndProductNameContainingIgnoreCase(user, searchTerm, pageable) >> filteredPage

        when:
            def result = this.service.getPriceAlerts(pageable, searchTerm, this.userPublicId)

        then:
            1 * priceAlertRepository.findByUserAndProductNameContainingIgnoreCase(user, searchTerm, pageable) >> filteredPage
            0 * priceAlertRepository.findByUser(_, _)

            result instanceof PriceAlertPage
            result.content.size() == 1
            result.content[0].product().name() == searchTerm
            result.totalElements == 1
    }

    def "Should return empty PriceAlertPage when search term matches nothing"() {
        given:
            def user = new UserEntity(publicId: this.userPublicId, username: "testuser")
            def searchTerm = "nonexistent"
            def pageable = PageRequest.of(0, 20)
            def emptyPage = new PageImpl<PriceAlertEntity>([], pageable, 0)
            this.priceAlertRepository.findByUserAndProductNameContainingIgnoreCase(user, searchTerm, pageable) >> emptyPage

        when:
            def result = this.service.getPriceAlerts(pageable, searchTerm, this.userPublicId)

        then:
            result instanceof PriceAlertPage
            result.content.isEmpty()
            result.totalElements == 0
    }

}
