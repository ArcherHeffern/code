package frog.dptb.client.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a blank constructor
@AllArgsConstructor // Generates a constructor for all fields
public abstract class Session {
    private Instant timestamp;
}
