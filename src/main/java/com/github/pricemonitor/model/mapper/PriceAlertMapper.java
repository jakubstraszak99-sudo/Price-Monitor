package com.github.pricemonitor.model.mapper;

import com.github.pricemonitor.model.dto.PriceAlertDto;
import com.github.pricemonitor.model.entity.PriceAlert;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PriceAlertMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "product.id", target = "productId")
    PriceAlertDto toDto(final PriceAlert alert);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "productId", target = "product.id")
    PriceAlert toEntity(final PriceAlertDto alertDto);

}