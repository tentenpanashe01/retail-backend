package com.company.retail.user.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserRequest {
    private String fullName;
    private String username;
    private String password;
    private List<String> roles;
    private Long shopId;
}