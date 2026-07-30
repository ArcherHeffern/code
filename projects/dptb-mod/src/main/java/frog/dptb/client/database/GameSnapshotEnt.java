package frog.dptb.client.database;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Table(name = "GameSnapshotEnt")
public class GameSnapshotEnt {
    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime time;
    private int playersOnline;
    private int gold;

    public GameSnapshotEnt(
            LocalDateTime time,
            int playersOnline,
            int gold
    ) {
        this.time = time;
        this.playersOnline = playersOnline;
        this.gold = gold;
    }
}
