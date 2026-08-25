package com.github.pricemonitor.model.mapper;

import com.github.pricemonitor.model.dto.Product;
import com.github.pricemonitor.model.entity.ProductEntity;
import org.mapstruct.Mapper;

import java.net.URI;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product map(final ProductEntity entity);

    ProductEntity map(final Product product);

    default URI mapStringToUri(final String str) {
        return str != null ? URI.create(str) : null;
    }

    default String mapUriToString(final URI uri) {
        return uri != null ? uri.toString() : null;
    }

}
