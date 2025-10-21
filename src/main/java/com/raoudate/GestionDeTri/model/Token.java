package com.raoudate.GestionDeTri.model;

import jakarta.persistence.*;
        import lombok.*;

        import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tokens")
public class Token {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(length = 1000)
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime validateAt;

    @ManyToOne
    @JoinColumn(name = "user_id" , nullable = false)
    private User user;
}
