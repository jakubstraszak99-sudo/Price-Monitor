package com.github.pricemonitor.service

import com.github.pricemonitor.exception.ExceptionCode
import com.github.pricemonitor.exception.PmRuntimeException
import com.github.pricemonitor.kafka.KafkaConstants
import com.github.pricemonitor.kafka.KafkaEventPublisher
import com.github.pricemonitor.kafka.message.ScraperReplyMessage
import com.github.pricemonitor.model.dto.ScrapedProduct
import com.github.pricemonitor.model.entity.PriceAlertEntity
import com.github.pricemonitor.model.entity.ProductEntity
import com.github.pricemonitor.model.mapper.ProductMapperImpl
import com.github.pricemonitor.model.page.ProductPage
import com.github.pricemonitor.repository.ProductRepository
import com.github.pricemonitor.service.impl.ProductServiceImpl
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import spock.lang.Specification
import spock.lang.Subject

import static com.github.pricemonitor.kafka.KafkaConstants.SCRAPER_REPLY_TOPIC

class ProductServiceSpec extends Specification {

    def productRepository = Mock(ProductRepository)
    def eventPublisher = Mock(KafkaEventPublisher)
    def priceHistoryService = Mock(PriceHistoryService)
    def priceAlertNotificationService = Mock(PriceAlertNotificationService)
    def productMapper = new ProductMapperImpl()

    @Subject
    def service = new ProductServiceImpl(
            this.productRepository,
            this.productMapper,
            this.eventPublisher,
            this.priceHistoryService,
            this.priceAlertNotificationService
    )

    def url = "https://example.com/product"
    def price = new BigDecimal("299.99")
    def scrapedData = new ScrapedProduct(
            "Test Product",
            new BigDecimal("299.99"),
            URI.create("http://image.url"),
            Currency.getInstance("PLN"),
            "Test Shop",
            URI.create("http://favicon.url"))

    def "Should return existing product entity when found in database"() {
        given:
            def existingProduct = new ProductEntity(productUrl: this.url, name: "Existing")
            this.productRepository.findByProductUrl(this.url) >> Optional.of(existingProduct)

        when:
            def result = this.service.getOrCreateProduct(this.url, this.scrapedData)

        then:
            result == existingProduct
            0 * this.productRepository.save(_)
            0 * this.priceHistoryService.createPriceHistory(_)
    }

    def "Should save new product when not found in database"() {
        given:
            this.productRepository.findByProductUrl(this.url) >> Optional.empty()
            def savedProduct = ProductEntity.builder()
                    .id(1L)
                    .productUrl(this.url)
                    .name("Test Product")
                    .build()

        when:
            def result = this.service.getOrCreateProduct(this.url, this.scrapedData)

        then:
            1 * this.priceHistoryService.createPriceHistory(savedProduct)
            1 * this.productRepository.save({ entity ->
                entity.productUrl == this.url &&
                        entity.name == "Test Product" &&
                        entity.currentPrice == this.price
            }) >> savedProduct
            result == savedProduct
    }

    def "Should return mapped ScrapedProduct directly from database and skip Kafka"() {
        given:
            def entity = new ProductEntity(productUrl: this.url, name: "Test Product DB", currentPrice: this.price)
            this.productRepository.findByProductUrl(this.url) >> Optional.of(entity)

        when:
            def result = this.service.getProductData(this.url)

        then:
            result.name() == "Test Product DB"
            result.price() == this.price
            0 * this.eventPublisher.publishAndReceive(*_)
    }

    def "Should fetch from Kafka when product is not in database"() {
        given:
            this.productRepository.findByProductUrl(this.url) >> Optional.empty()
            def replyMessage = new ScraperReplyMessage(this.url, this.scrapedData, true, null, null)

        when:
            def result = this.service.getProductData(this.url)

        then:
            result == this.scrapedData
            1 * this.eventPublisher.publishAndReceive(_, this.url, { request -> request.url() == this.url }) >> replyMessage
    }

    def "Should throw exception when Kafka returns failure"() {
        given:
            this.productRepository.findByProductUrl(this.url) >> Optional.empty()
            def replyMessage = new ScraperReplyMessage(this.url, this.scrapedData, false, "error", null)

        when:
            this.service.getProductData(this.url)

        then:
            1 * this.eventPublisher.publishAndReceive(_, this.url, { request -> request.url() == this.url }) >> replyMessage
            def e = thrown(PmRuntimeException)
            e.getCode() == ExceptionCode.E011
    }

    def "Should throw exception when Kafka returns success but scrapedProduct is null"() {
        given:
            this.productRepository.findByProductUrl(this.url) >> Optional.empty()
            def replyMessage = new ScraperReplyMessage(this.url, null, true, null, null)

        when:
            this.service.getProductData(this.url)

        then:
            1 * this.eventPublisher.publishAndReceive(_, this.url, { request -> request.url() == this.url }) >> replyMessage
            def e = thrown(PmRuntimeException)
            e.getCode() == ExceptionCode.E011
    }

    def "Should return paginated products"() {
        given:
            def pageable = PageRequest.of(0, 20)
            def entity1 = ProductEntity.builder()
                    .id(1L)
                    .productUrl(this.url)
                    .name("Product One")
                    .currentPrice(this.price)
                    .build()
            def entity2 = ProductEntity.builder()
                    .id(2L)
                    .productUrl("https://example.com/other")
                    .name("Product Two")
                    .currentPrice(new BigDecimal("49.99"))
                    .build()
            def entityPage = new PageImpl<ProductEntity>([entity1, entity2], pageable, 2)

        when:
            def result = this.service.getProducts(pageable, null)

        then:
            1 * this.productRepository.findAll({
                Pageable p -> p.pageNumber == 0 && p.pageSize == 20 &&
                        p.sort.getOrderFor("available")?.direction == Sort.Direction.DESC
            }) >> entityPage

            result instanceof ProductPage
            result.content.size() == 2
            result.content[0].name() == "Product One"
            result.content[1].name() == "Product Two"
            result.totalElements == 2
    }

    def "Should return empty ProductPage when no products found"() {
        given:
            def pageable = PageRequest.of(0, 20)
            def emptyPage = new PageImpl<ProductEntity>([], pageable, 0)

        when:
            def result = this.service.getProducts(pageable, null)

        then:
            1 * this.productRepository.findAll({ Pageable p ->
                p.sort.getOrderFor("available")?.direction == Sort.Direction.DESC
            }) >> emptyPage
            0 * this.productRepository.searchByNameOrShop(_, _)

            result instanceof ProductPage
            result.content.isEmpty()
            result.totalElements == 0
    }

    def "Should return filtered products when search term is provided"() {
        given:
            def searchTerm = "Product One"
            def pageable = PageRequest.of(0, 20)
            def entity1 = ProductEntity.builder()
                    .id(1L)
                    .productUrl(this.url)
                    .name("Product One")
                    .currentPrice(this.price)
                    .build()
            def filteredPage = new PageImpl<ProductEntity>([entity1], pageable, 1)
            this.productRepository.searchByNameOrShop(searchTerm, { Pageable p ->
                p.sort.getOrderFor("available")?.direction == Sort.Direction.DESC
            } as Pageable) >> filteredPage

        when:
            def result = this.service.getProducts(pageable, searchTerm)

        then:
            result instanceof ProductPage
            result.content.size() == 1
            result.content[0].name() == "Product One"
            result.totalElements == 1
            0 * this.productRepository.findAll(_)
        }

    def "Should return empty ProductPage when search term matches nothing"() {
        given:
            def searchTerm = "nonexistent"
            def pageable = PageRequest.of(0, 20)
            def emptyPage = new PageImpl<ProductEntity>([], pageable, 0)
            this.productRepository.searchByNameOrShop(searchTerm, { Pageable p ->
                p.sort.getOrderFor("available")?.direction == Sort.Direction.DESC
            } as Pageable) >> emptyPage


        when:
            def result = this.service.getProducts(pageable, searchTerm)

        then:
            result instanceof ProductPage
            result.content.isEmpty()
            result.totalElements == 0
    }

    def "Should call findAll instead of search when search term is blank"() {
        given:
            def pageable = PageRequest.of(0, 20)
            def entityPage = new PageImpl<ProductEntity>([], pageable, 0)

        when:
            this.service.getProducts(pageable, "   ")

        then:
            1 * this.productRepository.findAll({ Pageable p ->
                p.sort.getOrderFor("available")?.direction == Sort.Direction.DESC
            }) >> entityPage
            0 * this.productRepository.searchByNameOrShop(_, _)
    }

    def "Should keep requested sort as secondary, after available desc"() {
        given:
            def pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "name"))
            def entityPage = new PageImpl<ProductEntity>([], pageable, 0)

        when:
            this.service.getProducts(pageable, null)

        then:
            1 * this.productRepository.findAll({ Pageable p ->
                def orders = p.sort.toList()
                orders[0].property == "available" && orders[0].direction == Sort.Direction.DESC &&
                        orders[1].property == "name" && orders[1].direction == Sort.Direction.ASC
            }) >> entityPage
        }

    def "Should update product parameters and notify users when price changes"() {
        given:
            def existingProduct = new ProductEntity(
                    productUrl: this.url,
                    currentPrice: new BigDecimal("200.00"),
                    name: "Test",
                    imageUrl: "example.url.image.com",
                    faviconUrl: "example.url.favicon.com"
            )
            this.productRepository.findByProductUrl(this.url) >> Optional.of(existingProduct)

        when:
            this.service.updateProduct(this.url, this.scrapedData)

        then:
            1 * this.priceHistoryService.createPriceHistory(*_)
            1 * this.priceAlertNotificationService.notifyAboutPriceChange(existingProduct, scrapedData.price())
            existingProduct.currentPrice == this.price
            existingProduct.name == this.scrapedData.name()
            existingProduct.imageUrl == this.scrapedData.imageUrl().toString()
            existingProduct.faviconUrl == this.scrapedData.faviconUrl().toString()
    }

    def "Should not update product data when scraped data is identical"() {
        given:
        def existingProduct = new ProductEntity(
                productUrl: this.url,
                currentPrice: this.price,
                name: this.scrapedData.name(),
                imageUrl: this.scrapedData.imageUrl().toString(),
                faviconUrl: this.scrapedData.faviconUrl().toString()
        )
            this.productRepository.findByProductUrl(this.url) >> Optional.of(existingProduct)

        when:
            this.service.updateProduct(this.url, this.scrapedData)

        then:
            0 * this.priceHistoryService.createPriceHistory(*_)
            existingProduct.currentPrice == this.price
            existingProduct.name == this.scrapedData.name()
            existingProduct.imageUrl == this.scrapedData.imageUrl().toString()
            existingProduct.faviconUrl == this.scrapedData.faviconUrl().toString()
    }

    def "Should not update product data when scraped data is null"() {
        given:
            def existingProduct = new ProductEntity(
                    productUrl: this.url,
                    currentPrice: this.price,
                    name: this.scrapedData.name(),
                    imageUrl: this.scrapedData.imageUrl().toString(),
                    faviconUrl: this.scrapedData.faviconUrl().toString()
            )
            this.productRepository.findByProductUrl(this.url) >> Optional.of(existingProduct)

        when:
            this.service.updateProduct(this.url, new ScrapedProduct(null, null, null, null, null, null))

        then:
            0 * this.priceHistoryService.createPriceHistory(*_)
            existingProduct.currentPrice == this.price
            existingProduct.name == this.scrapedData.name()
            existingProduct.imageUrl == this.scrapedData.imageUrl().toString()
            existingProduct.faviconUrl == this.scrapedData.faviconUrl().toString()
    }

    def "Should mark product unavailable and remove its price alerts"() {
        given:
            def alert = new PriceAlertEntity()
            def existingProduct = new ProductEntity(
                    productUrl: this.url,
                    priceAlerts: [alert]
            )
            this.productRepository.findByProductUrl(this.url) >> Optional.of(existingProduct)

        when:
            this.service.markUnavailable(this.url)

        then:
            !existingProduct.getAvailable()
            existingProduct.getPriceAlerts().isEmpty()
    }


    def "Should do nothing when marking an unknown product unavailable"() {
        given:
            this.productRepository.findByProductUrl(this.url) >> Optional.empty()

        when:
            this.service.markUnavailable(this.url)

        then:
            noExceptionThrown()
    }

    def "Should mark product available again when it is successfully re-scraped"() {
        given:
            def existingProduct = new ProductEntity(
                    productUrl: this.url,
                    currentPrice: this.price,
                    name: this.scrapedData.name(),
                    imageUrl: this.scrapedData.imageUrl().toString(),
                    faviconUrl: this.scrapedData.faviconUrl().toString(),
                    available: false
            )
            this.productRepository.findByProductUrl(this.url) >> Optional.of(existingProduct)

        when:
            this.service.updateProduct(this.url, this.scrapedData)

        then:
            existingProduct.getAvailable()
    }

    def "Should publish async request for price check"() {
        when:
            this.service.requestProductCheck(this.url)

        then:
            1 * this.eventPublisher.publish(KafkaConstants.SCRAPER_REQUEST_TOPIC, this.url, { request -> request.url() == this.url }, SCRAPER_REPLY_TOPIC)
    }

}
