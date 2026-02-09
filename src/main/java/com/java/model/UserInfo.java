package com.java.model;

import javax.persistence.*;
import javax.validation.constraints.Email;
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
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @CreationTimestamp
    @Column(name = "created", nullable = false, updatable = false)
    private LocalDateTime created;

    @Column(name = "lastLogin", nullable = true, updatable = false)
    private LocalDateTime lastLogin;

    @Column(name = "name", nullable = true)
    private String name;

    @Column(unique = true)
    @Email(message = "Enter a valid email address.")
    private String email;

    private String password;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Column(name = "phones", nullable = true)
    @JoinColumn(name = "phone_id")
    private List<PhoneInfo> phones;

    @Transient
    private String token;

    private Boolean isActive;
}
