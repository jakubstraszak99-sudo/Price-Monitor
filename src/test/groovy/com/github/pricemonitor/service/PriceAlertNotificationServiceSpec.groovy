package com.github.pricemonitor.service

import com.github.pricemonitor.kafka.KafkaEventPublisher
import com.github.pricemonitor.kafka.message.EmailNotificationMessage
import com.github.pricemonitor.model.entity.PriceAlertEntity
import com.github.pricemonitor.model.entity.ProductEntity
import com.github.pricemonitor.model.entity.UserEntity
import com.github.pricemonitor.repository.PriceAlertRepository
import com.github.pricemonitor.service.impl.PriceAlertNotificationServiceImpl
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification
import spock.lang.Subject

class PriceAlertNotificationServiceSpec extends Specification {

    def priceAlertRepository = Mock(PriceAlertRepository)
    def eventPublisher = Mock(KafkaEventPublisher)
    def notificationService = Mock(NotificationService)

    @Subject
    def service = new PriceAlertNotificationServiceImpl(
            this.priceAlertRepository,
            this.eventPublisher,
            this.notificationService
    )

    def "Should notify user when product price reaches target price"() {
        given:
            def product = new ProductEntity(productUrl: "https://example.com/product")
            ReflectionTestUtils.setField(product, "id", 1L)
            def user = new UserEntity(email: "user@example.com")
            def alert = new PriceAlertEntity(user: user, active: true)
            def newPrice = new BigDecimal("199.99")

        when:
            this.service.notifyAboutPriceChange(product, newPrice)

        then:
            alert.getActive() == false
            1 * this.priceAlertRepository.findActiveAlertsForProduct(product.getId(), newPrice) >> Collections.singletonList(alert)
            1 * this.eventPublisher.publish(_, "user@example.com", {
                it instanceof EmailNotificationMessage &&
                        it.email() == "user@example.com" &&
                        it.item() == "https://example.com/product"
                }
            )
            1 * this.notificationService.notifyPriceDrop(user, product, newPrice)
    }

    def "Should not publish notification when there are no active alerts"() {
        given:
            def product = new ProductEntity(productUrl: "https://example.com/product")
            ReflectionTestUtils.setField(product, "id", 1L)
            def newPrice = new BigDecimal("199.99")

        when:
            this.service.notifyAboutPriceChange(product, newPrice)

        then:
            1 * this.priceAlertRepository.findActiveAlertsForProduct(product.getId(), newPrice) >> Collections.emptyList()
            0 * this.eventPublisher.publish(_, _, _)
            0 * this.notificationService.notifyPriceDrop(*_)
    }

    def "Should preserve in-app notifications and deactivate alerts when emails are disabled"() {
        given:
            def product = new ProductEntity(productUrl: "https://example.com/product")
            ReflectionTestUtils.setField(product, "id", 1L)
            def user = new UserEntity(email: "user@example.com", emailAlertsEnabled: false)
            def alert = new PriceAlertEntity(user: user, active: true)
            def newPrice = new BigDecimal("199.99")
            this.priceAlertRepository.findActiveAlertsForProduct(product.getId(), newPrice) >> [alert]

        when:
            this.service.notifyAboutPriceChange(product, newPrice)

        then:
            !alert.active
            0 * this.eventPublisher.publish(*_)
            1 * this.notificationService.notifyPriceDrop(user, product, newPrice)
    }

}
