package com.github.pricemonitor.model.dto;

import lombok.Data;

@Data
public class UserDto {

    private Long id;
    private String username;
    private String passwordHash;
    private String email;
    private Boolean isVerified;

}
