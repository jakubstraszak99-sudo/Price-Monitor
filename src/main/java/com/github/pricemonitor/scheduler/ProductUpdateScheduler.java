package com.github.pricemonitor.scheduler;

import com.github.pricemonitor.model.entity.ProductEntity;
import com.github.pricemonitor.repository.ProductRepository;
import com.github.pricemonitor.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductUpdateScheduler {

    private static final Integer BATCH_SIZE = 100;

    private final ProductService productService;
    private final ProductRepository productRepository;

    @Scheduled(cron = "0 0 * * * *")
    public void scheduleProductUpdates() {
        long lastId = 0;
        long queued = 0;
        List<ProductEntity> batch;

        do {
            batch = this.productRepository.findByIdGreaterThanOrderByIdAsc(lastId, PageRequest.of(0, BATCH_SIZE));
            for (final ProductEntity product : batch) {
                this.productService.requestProductCheck(product.getProductUrl());
                lastId = product.getId();
                queued++;
            }
        } while (batch.size() == BATCH_SIZE);

        log.info("Queued {} products for price check.", queued);
    }

}
