package com.github.pricemonitor.api.resource;

import com.github.pricemonitor.api.PriceAlertApi;
import com.github.pricemonitor.model.dto.PriceAlert;
import com.github.pricemonitor.model.page.PriceAlertPage;
import com.github.pricemonitor.model.request.alert.CreatePriceAlertRequest;
import com.github.pricemonitor.model.request.alert.UpdatePriceAlertRequest;
import com.github.pricemonitor.service.PriceAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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

    @Override
    public ResponseEntity<PriceAlertPage> getAlerts(final Pageable pageable, final String search, final UUID userPublicId) {
        final PriceAlertPage page = this.priceAlertService.getPriceAlerts(pageable, search, userPublicId);
        return ResponseEntity.status(HttpStatus.OK).body(page);
    }

    @Override
    public ResponseEntity<PriceAlert> updatePriceAlert(final UUID alertPublicId, final UpdatePriceAlertRequest request) {
        final PriceAlert alert = this.priceAlertService.updatePriceAlert(alertPublicId, request);
        return ResponseEntity.status(HttpStatus.OK).body(alert);
    }

    @Override
    public ResponseEntity<Void> deletePriceAlert(final UUID alertPublicId) {
        this.priceAlertService.deletePriceAlert(alertPublicId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
