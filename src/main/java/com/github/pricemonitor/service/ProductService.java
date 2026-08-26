package com.github.pricemonitor.service;

import com.github.pricemonitor.model.dto.ScrapedProduct;
import com.github.pricemonitor.model.entity.ProductEntity;

public interface ProductService {

    ProductEntity getOrCreateProduct(final String url, final ScrapedProduct data);

    ScrapedProduct getProductInfo(final String url);

}
