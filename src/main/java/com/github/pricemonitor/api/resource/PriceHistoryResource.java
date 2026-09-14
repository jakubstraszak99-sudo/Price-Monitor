package com.github.pricemonitor.api.resource;

import com.github.pricemonitor.api.PriceHistoryApi;
import com.github.pricemonitor.model.dto.PriceHistory;
import com.github.pricemonitor.service.PriceHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PriceHistoryResource implements PriceHistoryApi {

    private final PriceHistoryService priceHistoryService;

    @Override
    public ResponseEntity<List<PriceHistory>> getPriceHistory(final String productUrl) {
        final List<PriceHistory> priceHistoryList = this.priceHistoryService.getPriceHistory(productUrl);
        return ResponseEntity.status(HttpStatus.OK).body(priceHistoryList);
    }

}
