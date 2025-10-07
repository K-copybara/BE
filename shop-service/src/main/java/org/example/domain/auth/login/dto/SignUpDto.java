package org.example.domain.auth.login.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpDto {
    private String email;
    private String shopName;
    private String password;
    private String phone;
    private String address;
}

