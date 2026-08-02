package com.guanxuege.auth.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginDTO implements Serializable {
    private String phone;
    private String password;
}
