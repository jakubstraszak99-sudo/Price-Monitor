package com.github.pricemonitor.service;

import com.github.pricemonitor.model.dto.ScrapedProduct;

public interface ScraperService {

    ScrapedProduct scrapeProduct(final String url);

}
