package com.github.pricemonitor.api.resource;

import com.github.pricemonitor.api.ScraperApi;
import com.github.pricemonitor.model.dto.ScrapedProduct;
import com.github.pricemonitor.request.ScrapeRequest;
import com.github.pricemonitor.service.ProductScraperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ScraperResource implements ScraperApi {

    private final ProductScraperService productScraperService;

    @Override
    public ResponseEntity<ScrapedProduct> extractProductInfo(final ScrapeRequest request) {
        final ScrapedProduct scrapedProduct = this.productScraperService.scrapeProduct(request.url());
        return ResponseEntity.ok(scrapedProduct);
    }

}
