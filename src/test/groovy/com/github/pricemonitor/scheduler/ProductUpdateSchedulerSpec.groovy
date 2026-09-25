package com.github.pricemonitor.scheduler

import com.github.pricemonitor.model.entity.ProductEntity
import com.github.pricemonitor.repository.ProductRepository
import com.github.pricemonitor.service.ProductService
import org.springframework.data.domain.PageRequest
import spock.lang.Specification

class ProductUpdateSchedulerSpec extends Specification {
    def productService = Mock(ProductService)
    def productRepository = Mock(ProductRepository)
    def scheduler = new ProductUpdateScheduler(productService, productRepository)

    def "Should use the last ID instead of offsets so concurrent deletions do not skip products"() {
        given:
            def firstBatch = (1L..100L).collect {
                ProductEntity.builder().id(it).productUrl("https://example.com/product-$it").build()
            }
            def lastProduct = ProductEntity.builder().id(105L).productUrl("https://example.com/product-105").build()

        when:
            scheduler.scheduleProductUpdates()

        then:
            1 * productRepository.findByIdGreaterThanOrderByIdAsc(0L, PageRequest.of(0, 100)) >> firstBatch
            1 * productRepository.findByIdGreaterThanOrderByIdAsc(100L, PageRequest.of(0, 100)) >> [lastProduct]
            100 * productService.requestProductCheck({ it != lastProduct.productUrl })
            1 * productService.requestProductCheck(lastProduct.productUrl)
            0 * productRepository._
    }

    def "Should handle an empty database"() {
        when:
            scheduler.scheduleProductUpdates()

        then:
            1 * productRepository.findByIdGreaterThanOrderByIdAsc(0L, _) >> []
            0 * productService._
    }
}
