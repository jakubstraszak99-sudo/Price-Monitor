package com.github.pricemonitor.scheduler;

import com.github.pricemonitor.model.page.ProductPage;
import com.github.pricemonitor.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceUpdateScheduler {

    private static final Integer BATCH_SIZE = 100;

    private final ProductService productService;

    @Scheduled(cron = "0 0 * * * *")
    public void schedulePriceUpdates() {
        int pageNumber = 0;
        ProductPage page;

        do {
            page = this.productService.getProducts(PageRequest.of(pageNumber, BATCH_SIZE), null);
            page.getContent().forEach(product -> productService.requestPriceCheck(product.productUrl().toString()));
            pageNumber++;
        } while (page.hasNext());

        log.info("Queued {} products for price check.", page.getTotalElements());
    }

}
