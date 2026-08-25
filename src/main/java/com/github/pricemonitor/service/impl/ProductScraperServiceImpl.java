package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.model.dto.ScrapedProduct;
import com.github.pricemonitor.scraper.ShopScraper;
import com.github.pricemonitor.service.ProductScraperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.github.pricemonitor.exception.ExceptionCode.E011;

@Service
@RequiredArgsConstructor
public class ProductScraperServiceImpl implements ProductScraperService {

    private final List<ShopScraper> scrapers;

    @Override
    public ScrapedProduct scrapeProduct(final String url) {
        return this.scrapers.stream()
                .filter(scraper -> scraper.supports(url))
                .findFirst()
                .map(scraper -> scraper.scrape(url))
                .orElseThrow(() -> new PmRuntimeException(E011));
    }

}
