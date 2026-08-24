package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.model.dto.ScrapedProduct;
import com.github.pricemonitor.scraper.ShopScraper;
import com.github.pricemonitor.service.ProductScraperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static com.github.pricemonitor.exception.ExceptionCode.E009;
import static com.github.pricemonitor.exception.ExceptionCode.E010;

@Service
@RequiredArgsConstructor
public class ProductScraperServiceImpl implements ProductScraperService {

    private final List<ShopScraper> scrapers;

    @Override
    public ScrapedProduct scrapeProduct(final String url) {
        for (final ShopScraper scraper : this.scrapers) {
            if (scraper.supports(url)) {
                try {
                    return scraper.scrape(url);
                } catch (IOException e) {
                    throw new PmRuntimeException(E010);
                }
            }
        }

        throw new PmRuntimeException(E009);
    }

}
