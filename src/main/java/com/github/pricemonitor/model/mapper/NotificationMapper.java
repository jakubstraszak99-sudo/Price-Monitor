package com.github.pricemonitor.model.mapper;

import com.github.pricemonitor.model.dto.Notification;
import com.github.pricemonitor.model.entity.NotificationEntity;
import com.github.pricemonitor.model.entity.ProductEntity;
import com.github.pricemonitor.model.entity.UserEntity;
import com.github.pricemonitor.model.helper.NotificationType;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(
        componentModel = "spring",
        uses = ProductMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface NotificationMapper {

    Notification map(final NotificationEntity entity);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "product", source = "product")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "triggerPrice", source = "triggerPrice")
    @Mapping(target = "read", constant = "false")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    NotificationEntity map(final UserEntity user, final ProductEntity product, final NotificationType type, final BigDecimal triggerPrice);

}
