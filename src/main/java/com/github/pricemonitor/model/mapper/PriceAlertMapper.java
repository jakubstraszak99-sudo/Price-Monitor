package com.github.pricemonitor.model.mapper;

import com.github.pricemonitor.model.dto.PriceAlert;
import com.github.pricemonitor.model.entity.PriceAlertEntity;
import com.github.pricemonitor.model.entity.ProductEntity;
import com.github.pricemonitor.model.entity.UserEntity;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(
        componentModel = "spring",
        uses = ProductMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface PriceAlertMapper {

    PriceAlert map(final PriceAlertEntity entity);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "product", source = "product")
    @Mapping(target = "targetPrice", source = "targetPrice")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    PriceAlertEntity map(final UserEntity user, final ProductEntity product, final BigDecimal targetPrice);

}