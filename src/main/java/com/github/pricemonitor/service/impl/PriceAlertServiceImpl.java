package com.github.pricemonitor.service.impl;

import com.github.pricemonitor.model.dto.ScrapedProduct;
import com.github.pricemonitor.model.entity.PriceAlertEntity;
import com.github.pricemonitor.model.entity.ProductEntity;
import com.github.pricemonitor.model.entity.UserEntity;
import com.github.pricemonitor.model.mapper.PriceAlertMapper;
import com.github.pricemonitor.repository.PriceAlertRepository;
import com.github.pricemonitor.service.PriceAlertService;
import com.github.pricemonitor.service.ProductService;
import com.github.pricemonitor.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

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
        final UserEntity user = this.userService.getUser(userPublicId);
        final ProductEntity product = this.productService.getOrCreateProduct(url, scrapedProduct);
        final PriceAlertEntity alert = this.priceAlertMapper.map(user, product, targetPrice);
        this.priceAlertRepository.save(alert);
    }

}
