package com.github.pricemonitor.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification
import spock.lang.Subject

class JwtAuthenticationFilterSpec extends Specification {

    def tokenProvider = Mock(TokenProvider)
    def filterChain = Mock(FilterChain)

    @Subject
    def filter = new JwtAuthenticationFilter(this.tokenProvider)

    def request = new MockHttpServletRequest()
    def response = new MockHttpServletResponse()

    def setup() {
        SecurityContextHolder.clearContext()
    }

    def cleanup() {
        SecurityContextHolder.clearContext()
    }

    def "Should authenticate user when valid token is present in cookies"() {
        given:
            def token = "valid-jwt-token"
            def userPublicId = UUID.randomUUID()

            this.request.setCookies(new Cookie("accessToken", token))
            this.tokenProvider.extractUserPublicId(token) >> userPublicId

        when:
            this.filter.doFilterInternal(this.request, this.response, this.filterChain)

        then:
            def auth = SecurityContextHolder.getContext().getAuthentication()
            auth != null
            auth.getPrincipal() == userPublicId
            auth.getCredentials() == null
            auth.getAuthorities().isEmpty()

            1 * this.filterChain.doFilter(this.request, this.response)
    }

    def "Should ignore authentication when token cookie is missing"() {
        given:
            this.request.setCookies(null)

        when:
            this.filter.doFilterInternal(this.request, this.response, this.filterChain)

        then:
            SecurityContextHolder.getContext().getAuthentication() == null
            1 * this.filterChain.doFilter(this.request, this.response)
            0 * this.tokenProvider.extractUserPublicId(_)
    }

    def "Should ignore authentication when different cookies are present"() {
        given:
            this.request.setCookies(
                    new Cookie("JSESSIONID", "abc"),
                    new Cookie("someOtherCookie", "xyz")
            )

        when:
            this.filter.doFilterInternal(this.request, this.response, this.filterChain)

        then:
            SecurityContextHolder.getContext().getAuthentication() == null
            1 * this.filterChain.doFilter(this.request, this.response)
            0 * this.tokenProvider.extractUserPublicId(_)
    }

}
