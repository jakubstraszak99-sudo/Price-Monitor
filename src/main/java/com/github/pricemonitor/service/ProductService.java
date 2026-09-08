package com.github.pricemonitor.service;

import com.github.pricemonitor.model.dto.ScrapedProduct;
import com.github.pricemonitor.model.entity.ProductEntity;
import com.github.pricemonitor.model.page.ProductPage;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductEntity getOrCreateProduct(final String url, final ScrapedProduct data);

    ScrapedProduct getProductInfo(final String url);

    ProductPage getProducts(final Pageable pageable, final String search);

}
