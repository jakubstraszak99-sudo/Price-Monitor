package com.github.pricemonitor.model.mapper;

import com.github.pricemonitor.model.dto.ProductDto;
import com.github.pricemonitor.model.entity.Product;
import org.mapstruct.Mapper;

import java.net.URI;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDto toDto(final Product product);

    Product toEntity(final ProductDto productDto);

    default URI mapStringToUri(final String url) {
        if (url == null) {
            return null;
        }

        return URI.create(url);
    }

    default String mapUriToString(final URI uri) {
        if (uri == null) {
            return null;
        }

        return uri.toString();
    }

}
