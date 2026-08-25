package com.github.pricemonitor.service;

import com.github.pricemonitor.model.dto.ScrapedProduct;

public interface ProductInfoService {

    ScrapedProduct getProductInfo(final String url);

}
