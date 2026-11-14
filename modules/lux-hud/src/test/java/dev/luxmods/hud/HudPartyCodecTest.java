package dev.luxmods.hud;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class HudPartyCodecTest {
    @Test void roundTrips() {
        var member = new HudPartyCodec.Member(UUID.randomUUID(), "LuxFriend", 18, 20, 27);
        assertEquals(List.of(member), HudPartyCodec.decode(HudPartyCodec.encode(List.of(member))));
        assertTrue(HudPartyCodec.decode("").isEmpty());
    }

    @Test void skipsCorruptData() {
        assertTrue(HudPartyCodec.decode("garbage").isEmpty());
        assertTrue(HudPartyCodec.decode("a".repeat(513)).isEmpty());
        assertThrows(IllegalArgumentException.class, () ->
                new HudPartyCodec.Member(UUID.randomUUID(), "invalid|player", 20, 20, 1));
    }
}
