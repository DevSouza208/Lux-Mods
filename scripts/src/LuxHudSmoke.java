import dev.luxmods.hud.HudBarMath;
import dev.luxmods.hud.HudPartyCodec;
import java.util.List;
import java.util.UUID;

public final class LuxHudSmoke {
    private LuxHudSmoke() { }

    public static void main(String[] args) {
        if (HudBarMath.filledWidth(10, 20, 100) != 50
                || HudBarMath.filledWidth(-10, 20, 100) != 0
                || HudBarMath.filledWidth(999, 20, 100) != 100
                || HudBarMath.filledWidth(10, 0, 100) != 0) {
            throw new AssertionError("HUD bar clipping failed");
        }
        var original = new HudPartyCodec.Member(
                UUID.randomUUID(), "Teammate_1", 17, 20, 14);
        var decoded = HudPartyCodec.decode(HudPartyCodec.encode(List.of(original)));
        if (!decoded.equals(List.of(original))
                || !HudPartyCodec.decode("garbage").isEmpty()
                || !HudPartyCodec.decode("").isEmpty()) {
            throw new AssertionError("Party snapshot codec failed");
        }
        System.out.println("LUX HUD SMOKE PASSOU");
    }
}
