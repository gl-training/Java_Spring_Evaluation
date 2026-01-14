package com.java.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private String id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Email(message = "Enter a valid email address.")
    private String email;

    @Pattern(
            regexp = "^(?=^[^A-Z]*[A-Z][^A-Z]*$)(?=^([^\\d]*\\d[^\\d]*\\d[^\\d]*)$)(?!.*\\d\\d).{8,12}$",
            message = "Invalid password. It must contain only one uppercase letter and only two numbers, in combination with lowercase letters, with a length between 8 and 12 characters."
        )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PhoneDTO> phones;

    private LocalDateTime created;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime lastLogin;

    private String token;

    private Boolean isActive;
}
