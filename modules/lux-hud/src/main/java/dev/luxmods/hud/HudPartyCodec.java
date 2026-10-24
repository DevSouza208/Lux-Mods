package dev.luxmods.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Bounded, pure-Java wire format. No NBT or free-form display names.
 * Only the sender (server) decides which team members are disclosed.
 */
public final class HudPartyCodec {
    public static final int MAX_MEMBERS = 4;
    public static final int MAX_BYTES_AS_CHARS = 512;

    private HudPartyCodec() {
    }

    public record Member(UUID uuid, String name, float health, float maxHealth, int level) {
        public Member {
            if (uuid == null || name == null || !name.matches("[A-Za-z0-9_]{1,16}")
                    || !Float.isFinite(health) || !Float.isFinite(maxHealth)
                    || maxHealth <= 0 || maxHealth > 2048 || level < 0 || level > 32767) {
                throw new IllegalArgumentException("Invalid party member");
            }
            health = Math.max(0, Math.min(health, maxHealth));
        }
    }

    public static String encode(List<Member> members) {
        StringBuilder out = new StringBuilder();
        for (Member m : members.stream().limit(MAX_MEMBERS).toList()) {
            if (!out.isEmpty()) out.append('\n');
            out.append(m.uuid()).append('|').append(m.name()).append('|')
                    .append(String.format(Locale.ROOT, "%.2f", m.health())).append('|')
                    .append(String.format(Locale.ROOT, "%.2f", m.maxHealth())).append('|')
                    .append(m.level());
        }
        if (out.length() > MAX_BYTES_AS_CHARS) {
            throw new IllegalArgumentException("Party packet exceeds size limit");
        }
        return out.toString();
    }

    public static List<Member> decode(String message) {
        if (message == null || message.length() > MAX_BYTES_AS_CHARS || message.isBlank()) {
            return List.of();
        }
        List<Member> result = new ArrayList<>();
        for (String item : message.split("\n")) {
            if (result.size() >= MAX_MEMBERS) break;
            String[] v = item.split("\\|", -1);
            if (v.length != 5) continue;
            try {
                result.add(new Member(UUID.fromString(v[0]), v[1],
                        Float.parseFloat(v[2]), Float.parseFloat(v[3]),
                        Integer.parseInt(v[4])));
            } catch (IllegalArgumentException ignored) {
                // Bad member entry must never crash the HUD renderer.
            }
        }
        return List.copyOf(result);
    }
}
