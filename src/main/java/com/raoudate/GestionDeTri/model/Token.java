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

    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime validateAt;
    
    @Column(length = 500)
    private String temporaryPassword;

    @ManyToOne
    @JoinColumn(name = "user_id" , nullable = false)
    private User user;
}
