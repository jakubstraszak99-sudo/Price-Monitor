package com.github.pricemonitor.model.page;

import com.github.pricemonitor.model.dto.PriceAlert;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class PriceAlertPage extends PageImpl<PriceAlert> {

    public PriceAlertPage(final List<PriceAlert> content, final Pageable pageable, final long total) {
        super(content, pageable, total);
    }

}
