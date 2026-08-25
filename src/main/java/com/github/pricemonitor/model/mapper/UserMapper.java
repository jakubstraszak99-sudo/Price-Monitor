package com.github.pricemonitor.model.mapper;

import com.github.pricemonitor.model.dto.User;
import com.github.pricemonitor.model.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User map(final UserEntity entity);

    UserEntity map(final User user);

}
