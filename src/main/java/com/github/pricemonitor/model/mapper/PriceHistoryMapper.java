package com.github.pricemonitor.model.mapper;

import com.github.pricemonitor.model.dto.PriceHistoryDto;
import com.github.pricemonitor.model.entity.PriceHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PriceHistoryMapper {

    @Mapping(source = "product.id", target = "productId")
    PriceHistoryDto toDto(final PriceHistory history);

    @Mapping(source = "productId", target = "product.id")
    PriceHistory toEntity(final PriceHistoryDto historyDto);

}
