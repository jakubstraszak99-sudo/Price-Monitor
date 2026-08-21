package com.github.pricemonitor.model.mapper;

import com.github.pricemonitor.model.dto.UserDto;
import com.github.pricemonitor.model.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(final User user);

    User toEntity(final UserDto userDto);

}
