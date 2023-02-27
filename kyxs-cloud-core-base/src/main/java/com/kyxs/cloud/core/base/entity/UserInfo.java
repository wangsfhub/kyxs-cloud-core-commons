package com.kyxs.cloud.core.base.entity;

import lombok.Data;

@Data
public class UserInfo {
    private Long userId;
    private Long tenantId;
    private Long cusId;
    private String userName;
    private String phone;
    private String email;
    private String accessToken;

    public UserInfo(){

    }
}
