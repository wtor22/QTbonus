package com.quartztop.bonus.tokens;

import com.quartztop.bonus.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
@AllArgsConstructor
@Getter
@Setter
public class TokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "is_closed")
    private boolean isClosed;

    public TokenEntity() {
    }

    public TokenEntity(String token, UserEntity userEntity) {
        this.token = token;
        this.user = userEntity;
        this.expiryDate = LocalDateTime.now().plusMinutes(5);
    }

}
