package com.github.pricemonitor.model.mapper;

import com.github.pricemonitor.model.dto.Product;
import com.github.pricemonitor.model.dto.ScrapedProduct;
import com.github.pricemonitor.model.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.net.URI;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product map(final ProductEntity entity);

    @Mapping(target = "priceHistories", ignore = true)
    @Mapping(target = "priceAlerts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ProductEntity map(final Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productUrl", source = "url")
    @Mapping(target = "currentPrice", source = "data.price")
    @Mapping(target = "lastUpdated", expression = "java(LocalDateTime.now())")
    @Mapping(target = "priceHistories", ignore = true)
    @Mapping(target = "priceAlerts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ProductEntity map(final ScrapedProduct data, final String url);

    default URI mapStringToUri(final String str) {
        return str != null ? URI.create(str) : null;
    }

    default String mapUriToString(final URI uri) {
        return uri != null ? uri.toString() : null;
    }

}
