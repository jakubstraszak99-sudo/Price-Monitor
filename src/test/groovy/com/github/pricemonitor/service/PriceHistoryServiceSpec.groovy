package com.github.pricemonitor.service

import com.github.pricemonitor.model.dto.PriceHistory
import com.github.pricemonitor.model.entity.PriceHistoryEntity
import com.github.pricemonitor.model.entity.ProductEntity
import com.github.pricemonitor.model.mapper.PriceHistoryMapperImpl
import com.github.pricemonitor.repository.PriceHistoryRepository
import com.github.pricemonitor.service.impl.PriceHistoryServiceImpl
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

class PriceHistoryServiceSpec extends Specification {

    def priceHistoryRepository = Mock(PriceHistoryRepository)
    def priceHistoryMapper = new PriceHistoryMapperImpl()

    @Subject
    def service = new PriceHistoryServiceImpl(this.priceHistoryRepository, this.priceHistoryMapper)

    def "Should create price history with given price"() {
        given:
            def product = new ProductEntity()
            def price = new BigDecimal("19.99")

        when:
            this.service.createPriceHistory(product, price)

        then:
            1 * this.priceHistoryRepository.save({
            it.recordedPrice == price
                    && it.product == product
            })
    }

    def "Should create price history with product current price"() {
        given:
            def price = new BigDecimal("19.99")
            def product = ProductEntity.builder()
                    .currentPrice(price)
                    .build()

        when:
            this.service.createPriceHistory(product)

        then:
            1 * this.priceHistoryRepository.save({
                it.recordedPrice == price
                        && it.product == product
            })
    }

    def "Should return price history list"() {
        given:
            def productUrl = "https://example.com/product"
            def item1 = PriceHistoryEntity.builder()
                    .recordedPrice(new BigDecimal("12.00"))
                    .createdAt(LocalDateTime.of(2026, 12, 1, 10, 0))
                    .build()
            def item2 = PriceHistoryEntity.builder()
                    .recordedPrice(new BigDecimal("10.00"))
                    .createdAt(LocalDateTime.of(2026, 12, 3, 10, 0))
                    .build()
            def item3 = PriceHistoryEntity.builder()
                    .recordedPrice(new BigDecimal("8.00"))
                    .createdAt(LocalDateTime.of(2026, 12, 2, 10, 0))
                    .build()

            this.priceHistoryRepository.findByProductProductUrl(productUrl) >> List.of(item3, item1, item2)

        when:
            def result = this.service.getPriceHistory(productUrl)

        then:
            result == List.of(
                    new PriceHistory(new BigDecimal("12.00"), LocalDateTime.of(2026, 12, 1, 10, 0)),
                    new PriceHistory(new BigDecimal("8.00"), LocalDateTime.of(2026, 12, 2, 10, 0)),
                    new PriceHistory(new BigDecimal("10.00"), LocalDateTime.of(2026, 12, 3, 10, 0))
            )
    }

}
