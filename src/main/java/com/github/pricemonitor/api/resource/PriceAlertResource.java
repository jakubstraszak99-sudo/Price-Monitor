package com.github.pricemonitor.api.resource;

import com.github.pricemonitor.api.PriceAlertApi;
import com.github.pricemonitor.model.request.alert.CreatePriceAlertRequest;
import com.github.pricemonitor.service.PriceAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PriceAlertResource implements PriceAlertApi {

    private final PriceAlertService priceAlertService;

    @Override
    public ResponseEntity<Void> createAlert(final CreatePriceAlertRequest request, final UUID userPublicId) {
        this.priceAlertService.createPriceAlert(request.url(), request.targetPrice(), request.scrapedProduct(), userPublicId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
