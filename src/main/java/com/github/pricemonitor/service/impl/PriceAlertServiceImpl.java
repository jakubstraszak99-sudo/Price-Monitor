package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.model.dto.PriceAlert;
import com.github.pricemonitor.model.dto.ScrapedProduct;
import com.github.pricemonitor.model.entity.PriceAlertEntity;
import com.github.pricemonitor.model.entity.ProductEntity;
import com.github.pricemonitor.model.entity.UserEntity;
import com.github.pricemonitor.model.mapper.PriceAlertMapper;
import com.github.pricemonitor.model.page.PriceAlertPage;
import com.github.pricemonitor.model.request.alert.UpdatePriceAlertRequest;
import com.github.pricemonitor.repository.PriceAlertRepository;
import com.github.pricemonitor.service.PriceAlertService;
import com.github.pricemonitor.service.ProductService;
import com.github.pricemonitor.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static com.github.pricemonitor.exception.ExceptionCode.E014;
import static com.github.pricemonitor.exception.ExceptionCode.E015;

@Service
@RequiredArgsConstructor
public class PriceAlertServiceImpl implements PriceAlertService {

    private final PriceAlertRepository priceAlertRepository;
    private final ProductService productService;
    private final UserService userService;
    private final PriceAlertMapper priceAlertMapper;

    @Override
    @Transactional
    public void createPriceAlert(final String url,
                                 final BigDecimal targetPrice,
                                 final ScrapedProduct scrapedProduct,
                                 final UUID userPublicId) {
        if (this.priceAlertRepository.existsByUserPublicIdAndProductProductUrl(userPublicId, url)) {
            throw new PmRuntimeException(E014);
        }

        final UserEntity user = this.userService.getUserEntity(userPublicId);
        final ProductEntity product = this.productService.getOrCreateProduct(url, scrapedProduct);
        final PriceAlertEntity alert = this.priceAlertMapper.map(user, product, targetPrice);
        this.priceAlertRepository.save(alert);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkAlertExists(final UUID userPublicId, final String productUrl) {
        return this.priceAlertRepository.existsByUserPublicIdAndProductProductUrl(userPublicId, productUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public PriceAlertPage getPriceAlerts(final Pageable pageable,
                                         final String search,
                                         final UUID userPublicId) {
        final Page<PriceAlertEntity> alerts = (search != null && !search.isBlank())
                ? this.priceAlertRepository.searchAlertsByUserAndProductOrShop(userPublicId, search, pageable)
                : this.priceAlertRepository.findByUserPublicId(userPublicId, pageable);

        final Page<PriceAlert> page = alerts.map(this.priceAlertMapper::map);
        return new PriceAlertPage(page.getContent(), pageable, page.getTotalElements());
    }

    @Override
    @Transactional
    public PriceAlert updatePriceAlert(final UUID alertPublicId, final UpdatePriceAlertRequest request, final UUID userPublicId) {
        final PriceAlertEntity alert = this.priceAlertRepository.findByPublicIdAndUserPublicId(alertPublicId, userPublicId)
                .orElseThrow(() -> new PmRuntimeException(E015));

        if (request.targetPrice() != null) {
            alert.setTargetPrice(request.targetPrice());
        }

        if (request.active() != null) {
            alert.setActive(request.active());
        }

        return this.priceAlertMapper.map(alert);
    }

    @Override
    @Transactional
    public void deletePriceAlert(final UUID alertPublicId, final UUID userPublicId) {
        this.priceAlertRepository.deleteByPublicIdAndUserPublicId(alertPublicId, userPublicId);
    }

}
