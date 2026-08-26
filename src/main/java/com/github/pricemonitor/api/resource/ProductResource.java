package com.github.pricemonitor.api.resource;

import com.github.pricemonitor.api.ProductApi;
import com.github.pricemonitor.model.dto.ScrapedProduct;
import com.github.pricemonitor.model.request.product.ProductInfoRequest;
import com.github.pricemonitor.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductResource implements ProductApi {

    private final ProductService productService;

    @Override
    public ResponseEntity<ScrapedProduct> extractProductInfo(final ProductInfoRequest request) {
        final ScrapedProduct scrapedProduct = this.productService.getProductInfo(request.url());
        return ResponseEntity.ok(scrapedProduct);
    }

}
