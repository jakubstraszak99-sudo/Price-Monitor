package com.github.pricemonitor.service

import com.github.pricemonitor.exception.ExceptionCode
import com.github.pricemonitor.exception.PmRuntimeException
import com.github.pricemonitor.kafka.KafkaEventPublisher
import com.github.pricemonitor.kafka.message.ScraperReplyMessage
import com.github.pricemonitor.model.dto.ScrapedProduct
import com.github.pricemonitor.model.entity.ProductEntity
import com.github.pricemonitor.model.mapper.ProductMapperImpl
import com.github.pricemonitor.repository.ProductRepository
import com.github.pricemonitor.service.impl.ProductServiceImpl
import spock.lang.Specification
import spock.lang.Subject

class ProductServiceSpec extends Specification {

    def productRepository = Mock(ProductRepository)
    def kafkaEventPublisher = Mock(KafkaEventPublisher)
    def productMapper = new ProductMapperImpl()

    @Subject
    def service = new ProductServiceImpl(
            this.productRepository,
            this.productMapper,
            this.kafkaEventPublisher
    )

    def url = "https://example.com/product"
    def price = new BigDecimal("299.99")
    def scrapedData = new ScrapedProduct("Test Product", this.price, URI.create("http://image.url"), Currency.getInstance("PLN"))

    def "Should return existing product entity when found in database"() {
        given:
            def existingProduct = new ProductEntity(productUrl: this.url, name: "Existing")
            this.productRepository.findByProductUrl(this.url) >> Optional.of(existingProduct)

        when:
            def result = this.service.getOrCreateProduct(this.url, this.scrapedData)

        then:
            result == existingProduct
            0 * this.productRepository.save(_)
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
            1 * this.productRepository.save({ entity ->
                entity.productUrl == this.url &&
                        entity.name == "Test Product" &&
                        entity.currentPrice == this.price
            }) >> savedProduct
            result == savedProduct
    }

    def "Should return mapped ScrapedProduct directly from DB and skip Kafka"() {
        given:
            def entity = new ProductEntity(productUrl: this.url, name: "Test Product DB", currentPrice: this.price)
            this.productRepository.findByProductUrl(this.url) >> Optional.of(entity)

        when:
            def result = this.service.getProductInfo(this.url)

        then:
            result.name() == "Test Product DB"
            result.price() == this.price
            0 * this.kafkaEventPublisher.publishAndReceive(*_)
    }

    def "Should fetch from Kafka when product is not in database"() {
        given:
            this.productRepository.findByProductUrl(this.url) >> Optional.empty()
            def replyMessage = new ScraperReplyMessage(this.scrapedData, true, null)

        when:
            def result = this.service.getProductInfo(this.url)

        then:
            result == this.scrapedData
            1 * this.kafkaEventPublisher.publishAndReceive(_, this.url, { request -> request.url() == this.url }) >> replyMessage
    }

    def "Should throw exception when Kafka returns failure"() {
        given:
            this.productRepository.findByProductUrl(this.url) >> Optional.empty()
            def replyMessage = new ScraperReplyMessage(this.scrapedData, false, "error")

        when:
            this.service.getProductInfo(this.url)

        then:
            1 * this.kafkaEventPublisher.publishAndReceive(_, this.url, { request -> request.url() == this.url }) >> replyMessage
            def e = thrown(PmRuntimeException)
            e.getCode() == ExceptionCode.E011
    }

    def "Should throw exception when Kafka returns success but scrapedProduct is null"() {
        given:
            this.productRepository.findByProductUrl(this.url) >> Optional.empty()
            def replyMessage = new ScraperReplyMessage(null, true, null)

        when:
            this.service.getProductInfo(this.url)

        then:
            1 * this.kafkaEventPublisher.publishAndReceive(_, this.url, { request -> request.url() == this.url }) >> replyMessage
            def e = thrown(PmRuntimeException)
            e.getCode() == ExceptionCode.E011
    }

}
