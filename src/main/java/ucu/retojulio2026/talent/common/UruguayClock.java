package ucu.retojulio2026.talent.common;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class UruguayClock {

    public static final ZoneId ZONA_URUGUAY = ZoneId.of("America/Montevideo");

    private UruguayClock() {
    }

    public static LocalDateTime ahora() {
        return LocalDateTime.now(ZONA_URUGUAY);
    }
}
