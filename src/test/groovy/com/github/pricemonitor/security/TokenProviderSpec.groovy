package com.github.pricemonitor.security

import com.github.pricemonitor.exception.ExceptionCode
import com.github.pricemonitor.exception.PmRuntimeException
import com.github.pricemonitor.properties.AppProperties
import spock.lang.Specification
import spock.lang.Subject

class TokenProviderSpec extends Specification {

    def secret = "Testc3VwZXItc2VjcmV0LWtleS10aGF0LWlzLWF0LWxlYXN0LTMyLWJ5dGVzLWxvbmch"
    def verificationExpMs = 3600000L
    def accessExpMs = 900000L
    def refreshExpMs = 86400000L
    def appProperties = new AppProperties(null, new AppProperties.Jwt(this.secret, this.verificationExpMs, this.accessExpMs, this.refreshExpMs), null)

    @Subject
    def tokenProvider = new TokenProvider(this.appProperties)

    def userPublicId = UUID.randomUUID()

    def "Should generate and extract valid access token"() {
        when:
            def token = this.tokenProvider.generateAccessToken(this.userPublicId)
            def extractedId = this.tokenProvider.extractUserPublicId(token)

        then:
            extractedId == this.userPublicId
    }

    def "Should generate and extract valid verification token"() {
        when:
            def token = this.tokenProvider.generateVerificationToken(this.userPublicId)
            def extractedId = this.tokenProvider.extractUserPublicId(token)

        then:
            extractedId == this.userPublicId
    }

    def "Should generate valid refresh token"() {
        when:
            def refreshToken = this.tokenProvider.generateRefreshToken(this.userPublicId)

        then:
            refreshToken.getTokenId() != null
            refreshToken.getUserPublicId() == this.userPublicId
            refreshToken.getExpirationInSeconds() == this.refreshExpMs / 1000
    }

    def "Should throw E005 when trying to extract ID from expired token"() {
        given:
        def expiredProperties = new AppProperties(null, new AppProperties.Jwt(this.secret, -600000L, -600000L, -600000L), null)
        def expiredProvider = new TokenProvider(expiredProperties)
        def expiredToken = expiredProvider.generateAccessToken(this.userPublicId)

        when:
            this.tokenProvider.extractUserPublicId(expiredToken)

        then:
            def e = thrown(PmRuntimeException)
            e.getCode() == ExceptionCode.E005
    }

    def "Should throw E006 when trying to extract ID from token with invalid signature"() {
        given:
            def hackerSecret = "aGFrZXJza2ktc2VjcmV0LWtleS10aGF0LWlzLWF0LWxlYXN0LTMyLWJ5dGVzLWxvbmch"
            def hackerProperties = new AppProperties(null, new AppProperties.Jwt(hackerSecret, this.accessExpMs, this.accessExpMs, this.refreshExpMs), null)
            def hackerProvider = new TokenProvider(hackerProperties)
            def forgedToken = hackerProvider.generateAccessToken(this.userPublicId)

        when:
            this.tokenProvider.extractUserPublicId(forgedToken)

        then:
            def e = thrown(PmRuntimeException)
            e.getCode() == ExceptionCode.E006
    }

    def "Should return correct access expiration in seconds"() {
        expect:
            (BigDecimal) this.tokenProvider.getAccessExpirationInSeconds() == this.accessExpMs / 1000
    }

}
