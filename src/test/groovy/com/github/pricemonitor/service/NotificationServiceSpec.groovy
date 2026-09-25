package com.github.pricemonitor.service

import com.github.pricemonitor.model.dto.Notification
import com.github.pricemonitor.model.entity.NotificationEntity
import com.github.pricemonitor.model.entity.ProductEntity
import com.github.pricemonitor.model.entity.UserEntity
import com.github.pricemonitor.model.mapper.NotificationMapperImpl
import com.github.pricemonitor.model.mapper.ProductMapperImpl
import com.github.pricemonitor.repository.NotificationRepository
import com.github.pricemonitor.service.impl.NotificationServiceImpl
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.messaging.simp.SimpMessagingTemplate
import spock.lang.Specification
import spock.lang.Subject

import static com.github.pricemonitor.model.helper.NotificationType.*

class NotificationServiceSpec extends Specification {

    def notificationRepository = Mock(NotificationRepository)
    def notificationMapper = new NotificationMapperImpl(new ProductMapperImpl())
    def messagingTemplate = Mock(SimpMessagingTemplate)

    @Subject
    def service = new NotificationServiceImpl(this.notificationRepository, this.notificationMapper, this.messagingTemplate)

    def userPublicId = UUID.randomUUID()

    def "Should save a PRICE_DROP notification and push it over the websocket"() {
        given:
            def user = new UserEntity(publicId: this.userPublicId)
            def product = new ProductEntity(productUrl: "https://example.com/product")
            def triggerPrice = new BigDecimal("199.99")

        when:
            this.service.notifyPriceDrop(user, product, triggerPrice)

        then:
            1 * this.notificationRepository.save({ NotificationEntity n ->
                n.user == user && n.product == product &&
                        n.type == PRICE_DROP &&
                        n.triggerPrice == triggerPrice &&
                        !n.read
            }) >> { NotificationEntity n -> n }
            1 * this.messagingTemplate.convertAndSendToUser(this.userPublicId.toString(), "/queue/notifications", { Notification dto ->
                dto.type() == PRICE_DROP && dto.triggerPrice() == triggerPrice
            })
    }


    def "Should save a PRODUCT_UNAVAILABLE notification and push it over the websocket"() {
        given:
            def user = new UserEntity(publicId: this.userPublicId)
            def product = new ProductEntity(productUrl: "https://example.com/product")

        when:
            this.service.notifyProductUnavailable(user, product)

        then:
            1 * this.notificationRepository.save({ NotificationEntity n ->
                n.user == user && n.product == product &&
                        n.type == PRODUCT_UNAVAILABLE &&
                        n.triggerPrice == null
            }) >> { NotificationEntity n -> n }
            1 * this.messagingTemplate.convertAndSendToUser(this.userPublicId.toString(), "/queue/notifications", { Notification dto ->
                dto.type() == PRODUCT_UNAVAILABLE
            })
    }


    def "Should return mapped notifications page for the given user"() {
        given:
            def pageable = PageRequest.of(0, 20)
            def product = new ProductEntity(productUrl: "https://example.com/product", name: "Test")
            def entity = NotificationEntity.builder()
                    .user(new UserEntity())
                    .product(product)
                    .type(PRICE_DROP)
                    .triggerPrice(new BigDecimal("99.99"))
                    .build()
            def entityPage = new PageImpl<NotificationEntity>([entity], pageable, 1)
            this.notificationRepository.findByUserPublicIdOrderByCreatedAtDesc(this.userPublicId, pageable) >> entityPage

        when:
            def result = this.service.getNotifications(pageable, this.userPublicId)

        then:
            result.content.size() == 1
            result.content[0].type() == PRICE_DROP
            result.totalElements == 1
    }

    def "Should return unread count for the given user"() {
        given:
            this.notificationRepository.countByUserPublicIdAndReadFalse(this.userPublicId) >> 3L

        expect:
            this.service.getUnreadCount(this.userPublicId) == 3L
    }

    def "Should mark a single notification as read when it belongs to the user"() {
        given:
            def notificationPublicId = UUID.randomUUID()
            def notification = new NotificationEntity(read: false)
            this.notificationRepository.findByPublicIdAndUserPublicId(notificationPublicId, this.userPublicId) >> Optional.of(notification)

        when:
            this.service.markAsRead(notificationPublicId, this.userPublicId)

        then:
            notification.isRead()
    }

    def "Should do nothing when marking a non-existent or foreign notification as read"() {
        given:
            def notificationPublicId = UUID.randomUUID()
            this.notificationRepository.findByPublicIdAndUserPublicId(notificationPublicId, this.userPublicId) >> Optional.empty()

        when:
            this.service.markAsRead(notificationPublicId, this.userPublicId)

        then:
            noExceptionThrown()
    }

    def "Should mark all notifications as read for the given user"() {
        when:
            this.service.markAsRead(null, this.userPublicId)

        then:
            1 * this.notificationRepository.markAllAsRead(this.userPublicId)
    }

    def "Should delete a single notification when it belongs to the user"() {
        given:
            def notificationPublicId = UUID.randomUUID()

        when:
            this.service.deleteNotification(notificationPublicId, this.userPublicId)

        then:
            1 * this.notificationRepository.deleteByPublicIdAndUserPublicId(notificationPublicId, this.userPublicId)
    }

    def "Should delete all notifications for the given user"() {
        when:
            this.service.deleteNotification(null, this.userPublicId)

        then:
            1 * this.notificationRepository.deleteByUserPublicId(this.userPublicId)
    }

    def "Should save a PRODUCT_UNAVAILABLE notification with a null product reference and push it over the websocket when product is removed"() {
        given:
            def user = new UserEntity(publicId: this.userPublicId)
            def product = new ProductEntity(productUrl: "https://example.com/product")

        when:
            this.service.notifyProductRemoved(user, product)

        then:
            1 * this.notificationRepository.save({ NotificationEntity n ->
                n.user == user &&
                        n.product == null &&
                        n.type == PRODUCT_UNAVAILABLE &&
                        n.triggerPrice == null
            }) >> { NotificationEntity n -> n }

            1 * this.messagingTemplate.convertAndSendToUser(this.userPublicId.toString(), "/queue/notifications", { Notification dto ->
                dto.type() == PRODUCT_UNAVAILABLE
            })
    }

}

