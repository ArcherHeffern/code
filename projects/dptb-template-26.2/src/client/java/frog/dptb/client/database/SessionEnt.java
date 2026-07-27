package frog.dptb.client.database;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "SessionEnt")
@NoArgsConstructor
public class SessionEnt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime begin;
    private LocalDateTime end;

    public SessionEnt(LocalDateTime begin, LocalDateTime end) {
        this.begin = begin;
        this.end = end;
    }
}
