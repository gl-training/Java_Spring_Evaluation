package com.java.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

	@Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created", nullable = false, updatable = false)
    private LocalDateTime created;

    @Column(name = "lastLogin", nullable = true, updatable = false)
    private LocalDateTime lastLogin;

    @Column(name = "name", nullable = true)
	@Size(message = "Enter minimum 3 character and maximum 20 characters in full name.")
    private String name;

    @Column(unique = true)
    @Email(message = "Enter a valid email address.")
    private String email;

    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Invalid password. It must contain at least 8 characters, including at least one digit, one lowercase letter, one uppercase letter, and one special character."
        )
    private String password;

    @OneToMany(cascade = CascadeType.ALL)
    @Column(name = "phones", nullable = true)
    @JoinColumn(name = "phone_id")
    private List<PhoneInfo> phones;

    @Transient
    private String token;

    private Boolean isActive;
}
