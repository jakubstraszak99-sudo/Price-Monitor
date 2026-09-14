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
import com.github.pricemonitor.model.request.alert.UpdatePriceAlertRequest
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

    def "Should successfully create and save price alert"() {
        given:
            def userPublicId = UUID.randomUUID()
            def testUser = new UserEntity(publicId: userPublicId, username: "testuser")
            def url = "https://example.com/product"
            def targetPrice = new BigDecimal("1500.00")
            def scrapedProduct = new ScrapedProduct(
                    "Test Product",
                    new BigDecimal("2000.00"),
                    URI.create("http://image.url"),
                    Currency.getInstance("PLN"),
                    "Test Shop",
                    URI.create("http://favicon.url"))
            def user = new UserEntity(publicId: userPublicId, username: "testuser")
            def product = new ProductEntity(productUrl: url, name: "Test Product")

        this.userService.getUserEntity(userPublicId) >> testUser
            this.productService.getOrCreateProduct(url, scrapedProduct) >> product

        when:
            this.service.createPriceAlert(url, targetPrice, scrapedProduct, userPublicId)

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
            def userPublicId = UUID.randomUUID()
            def testUser = new UserEntity(publicId: userPublicId, username: "testuser")
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

            this.userService.getUserEntity(userPublicId) >> testUser
            this.productService.getOrCreateProduct(url, scrapedProduct) >> product
            this.priceAlertRepository.existsByUserAndProduct(testUser, product) >> true

        when:
            this.service.createPriceAlert(url, targetPrice, scrapedProduct, userPublicId)

        then:
            def e = thrown(PmRuntimeException)
            e.getCode() == ExceptionCode.E014
            0 * this.priceAlertRepository.save(_)
    }

    def "Should return paginated price alerts"() {
        given:
            def userPublicId = UUID.randomUUID()
            def testUser = new UserEntity(publicId: userPublicId, username: "testuser")
            def pageable = PageRequest.of(0, 20)
            def alertEntity = new PriceAlertEntity()
            def entitiesPage = new PageImpl<>([alertEntity], pageable, 1)

            this.userService.getUserEntity(userPublicId) >> testUser

        when:
            def result = this.service.getPriceAlerts(pageable, null, userPublicId)

        then:
            1 * priceAlertRepository.findByUser(testUser, pageable) >> entitiesPage
            0 * priceAlertRepository.findByUserAndProductNameContainingIgnoreCase(_, _, _)

            result instanceof PriceAlertPage
            result.content.size() == 1
            result.totalElements == 1
    }

    def "Should return filtered price alerts when search term is provided"() {
        given:
            def userPublicId = UUID.randomUUID()
            def testUser = new UserEntity(publicId: userPublicId, username: "testuser")
            def searchTerm = "Test"
            def product = ProductEntity.builder()
                    .id(1L)
                    .name("Test")
                    .build()
            def pageable = PageRequest.of(0, 20)
            def alertEntity = new PriceAlertEntity()
            alertEntity.setProduct(product)
            def filteredPage = new PageImpl<PriceAlertEntity>([alertEntity], pageable, 1)

            this.userService.getUserEntity(userPublicId) >> testUser
            this.priceAlertRepository.findByUserAndProductNameContainingIgnoreCase(testUser, searchTerm, pageable) >> filteredPage

        when:
            def result = this.service.getPriceAlerts(pageable, searchTerm, userPublicId)

        then:
            1 * priceAlertRepository.findByUserAndProductNameContainingIgnoreCase(testUser, searchTerm, pageable) >> filteredPage
            0 * priceAlertRepository.findByUser(_, _)

            result instanceof PriceAlertPage
            result.content.size() == 1
            result.content[0].product().name() == searchTerm
            result.totalElements == 1
    }

    def "Should return empty PriceAlertPage when search term matches nothing"() {
        given:
            def userPublicId = UUID.randomUUID()
            def testUser = new UserEntity(publicId: userPublicId, username: "testuser")
            def searchTerm = "nonexistent"
            def pageable = PageRequest.of(0, 20)
            def emptyPage = new PageImpl<PriceAlertEntity>([], pageable, 0)

        this.userService.getUserEntity(userPublicId) >> testUser
        this.priceAlertRepository.findByUserAndProductNameContainingIgnoreCase(testUser, searchTerm, pageable) >> emptyPage

        when:
            def result = this.service.getPriceAlerts(pageable, searchTerm, userPublicId)

        then:
            result instanceof PriceAlertPage
            result.content.isEmpty()
            result.totalElements == 0
    }

    def "Should successfully toggle price alert status from #initialState to #expectedState"() {
        given:
            def alertPublicId = UUID.randomUUID()
            def alertEntity = new PriceAlertEntity(active: initialState)
            def request = new UpdatePriceAlertRequest(!initialState, null)

        when:
            def result = this.service.updatePriceAlert(alertPublicId, request)

        then:
            1 * this.priceAlertRepository.findByPublicId(alertPublicId) >> Optional.of(alertEntity)
            alertEntity.getActive() == expectedState
            result != null
            result.active() == expectedState

        where:
            initialState || expectedState
            true         || false
            false        || true
    }

    def "Should update target price successfully"() {
        given:
            def alertPublicId = UUID.randomUUID()
            def oldPrice = new BigDecimal(39.99)
            def newPrice = new BigDecimal(29.99)
            def alertEntity = new PriceAlertEntity(targetPrice: oldPrice)
            def request = new UpdatePriceAlertRequest(null, newPrice)

        when:
            this.service.updatePriceAlert(alertPublicId, request)

        then:
            1 * this.priceAlertRepository.findByPublicId(alertPublicId) >> Optional.of(alertEntity)
            alertEntity.getTargetPrice() == newPrice
    }

    def "Should throw E015 when toggling status of non-existent price alert"() {
        given:
            def alertPublicId = UUID.randomUUID()

        when:
            this.service.updatePriceAlert(alertPublicId, null)

        then:
            1 * this.priceAlertRepository.findByPublicId(alertPublicId) >> Optional.empty()
            def e = thrown(PmRuntimeException)
            e.getCode() == ExceptionCode.E015
    }

    def "Should delete price alert properly"() {
        given:
            def alertPublicId = UUID.randomUUID()

        when:
            this.service.deletePriceAlert(alertPublicId)

        then:
            1 * this.priceAlertRepository.deleteByPublicId(alertPublicId)
    }

}
