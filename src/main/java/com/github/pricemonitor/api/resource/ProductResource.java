package com.github.pricemonitor.api.resource;

import com.github.pricemonitor.api.ProductApi;
import com.github.pricemonitor.model.dto.ScrapedProduct;
import com.github.pricemonitor.model.request.product.ProductInfoRequest;
import com.github.pricemonitor.service.ProductInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductResource implements ProductApi {

    private final ProductInfoService productInfoService;

    @Override
    public ResponseEntity<ScrapedProduct> extractProductInfo(final ProductInfoRequest request) {
        final ScrapedProduct scrapedProduct = this.productInfoService.getProductInfo(request.url());
        return ResponseEntity.ok(scrapedProduct);
    }

}
