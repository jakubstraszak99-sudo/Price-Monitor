package com.github.pricemonitor.model.mapper;

import com.github.pricemonitor.model.dto.User;
import com.github.pricemonitor.model.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User map(final UserEntity entity);

}
