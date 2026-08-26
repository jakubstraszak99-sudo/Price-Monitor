package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.model.dto.ScrapedProduct;
import com.github.pricemonitor.model.entity.ProductEntity;
import com.github.pricemonitor.model.mapper.ProductMapper;
import com.github.pricemonitor.repository.ProductRepository;
import com.github.pricemonitor.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductEntity> findProduct(final String url) {
        return this.productRepository.findByProductUrl(url);
    }

    @Override
    @Transactional
    public ProductEntity getOrCreateProduct(final String url, final ScrapedProduct data) {
        return this.findProduct(url).orElseGet(() -> this.createProduct(url, data));
    }

    private ProductEntity createProduct(final String url, final ScrapedProduct data) {
        final ProductEntity product = this.productMapper.map(data, url);
        return this.productRepository.save(product);
    }

}
