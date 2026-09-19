package com.github.pricemonitor.model.page;

import com.github.pricemonitor.model.dto.Notification;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class NotificationPage extends PageImpl<Notification> {

    public NotificationPage(final List<Notification> content, final Pageable pageable, final long total) {
        super(content, pageable, total);
    }

}
