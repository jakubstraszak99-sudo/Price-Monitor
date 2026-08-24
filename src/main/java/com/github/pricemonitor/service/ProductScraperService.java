package com.github.pricemonitor.service;

import com.github.pricemonitor.model.dto.ScrapedProduct;

public interface ProductScraperService {

    ScrapedProduct scrapeProduct(final String url);

}
