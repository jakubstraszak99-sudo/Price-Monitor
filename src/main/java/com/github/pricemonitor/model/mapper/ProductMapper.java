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

    @Mapping(target = "productUrl", source = "url")
    @Mapping(target = "currentPrice", source = "data.price")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastUpdated", ignore = true)
    @Mapping(target = "available", ignore = true)
    @Mapping(target = "priceHistories", ignore = true)
    @Mapping(target = "priceAlerts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    ProductEntity map(final ScrapedProduct data, final String url);

    @Mapping(target = "price", source = "currentPrice")
    ScrapedProduct mapToScrapedProduct(final ProductEntity entity);

    default URI mapStringToUri(final String str) {
        return str != null ? URI.create(str) : null;
    }

    default String mapUriToString(final URI uri) {
        return uri != null ? uri.toString() : null;
    }

}
