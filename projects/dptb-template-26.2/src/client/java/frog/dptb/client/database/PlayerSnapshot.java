package frog.dptb.client.database;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "PlayerSnapshot")
public class PlayerSnapshot {
    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime time;
    private int numPlayers;

    public PlayerSnapshot() {

    }

    public PlayerSnapshot(
            LocalDateTime time,
            int numPlayers
    ) {
        this.time = time;
        this.numPlayers = numPlayers;
    }
}
