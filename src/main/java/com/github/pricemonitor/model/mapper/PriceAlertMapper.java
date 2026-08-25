package com.github.pricemonitor.model.mapper;

import com.github.pricemonitor.model.dto.PriceAlert;
import com.github.pricemonitor.model.entity.PriceAlertEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PriceAlertMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "product.id", target = "productId")
    PriceAlert map(final PriceAlertEntity entity);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "productId", target = "product.id")
    PriceAlertEntity map(final PriceAlert alert);

}