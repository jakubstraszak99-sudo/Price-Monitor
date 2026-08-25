package com.github.pricemonitor.model.mapper;

import com.github.pricemonitor.model.dto.PriceHistory;
import com.github.pricemonitor.model.entity.PriceHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PriceHistoryMapper {

    PriceHistory map(final PriceHistoryEntity entity);

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    PriceHistoryEntity map(final PriceHistory history);

}
