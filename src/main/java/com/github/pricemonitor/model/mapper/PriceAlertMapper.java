package com.github.pricemonitor.model.mapper;

import com.github.pricemonitor.model.dto.PriceAlert;
import com.github.pricemonitor.model.entity.PriceAlertEntity;
import com.github.pricemonitor.model.entity.ProductEntity;
import com.github.pricemonitor.model.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface PriceAlertMapper {

    @Mapping(target = "productId", source = "product.id")
    PriceAlert map(final PriceAlertEntity entity);

    @Mapping(target = "product.id", source = "productId")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    PriceAlertEntity map(final PriceAlert alert);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "product", source = "product")
    @Mapping(target = "targetPrice", source = "targetPrice")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    PriceAlertEntity map(final UserEntity user, final ProductEntity product, final BigDecimal targetPrice);

}