package com.github.pricemonitor.service;

import com.github.pricemonitor.model.dto.ScrapedProduct;
import com.github.pricemonitor.model.entity.ProductEntity;

import java.util.Optional;

public interface ProductService {

    Optional<ProductEntity> findProduct(final String url);

    ProductEntity getOrCreateProduct(final String url, final ScrapedProduct data);

}
