package com.github.pricemonitor.model.mapper;

import com.github.pricemonitor.model.dto.PriceHistory;
import com.github.pricemonitor.model.entity.PriceHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PriceHistoryMapper {

    @Mapping(source = "product.id", target = "productId")
    PriceHistory map(final PriceHistoryEntity entity);

    @Mapping(source = "productId", target = "product.id")
    PriceHistoryEntity map(final PriceHistory history);

}
