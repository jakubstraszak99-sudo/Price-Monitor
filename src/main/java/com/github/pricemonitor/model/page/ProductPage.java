package com.github.pricemonitor.model.page;

import com.github.pricemonitor.model.dto.Product;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class ProductPage extends PageImpl<Product> {

    public ProductPage(final List<Product> content, final Pageable pageable, final long total) {
        super(content, pageable, total);
    }

}
