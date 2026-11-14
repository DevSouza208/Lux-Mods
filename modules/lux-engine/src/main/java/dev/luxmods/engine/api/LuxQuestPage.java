package dev.luxmods.engine.api;

import java.util.List;
import java.util.Objects;

/** Stateless, pure Java pagination for the compact Lux Atlas mission sidebar. */
public final class LuxQuestPage {
    private LuxQuestPage() {}

    public static int pages(int total, int pageSize) {
        if (total < 0 || pageSize < 1) throw new IllegalArgumentException("Invalid pagination");
        return total == 0 ? 1 : 1 + (total - 1) / pageSize;
    }

    public static int clamp(int requested, int total, int pageSize) {
        return Math.max(0, Math.min(requested, pages(total, pageSize) - 1));
    }

    public static <T> List<T> slice(List<T> values, int requested, int pageSize) {
        Objects.requireNonNull(values, "values");
        int page = clamp(requested, values.size(), pageSize);
        int first = page * pageSize;
        return List.copyOf(values.subList(first, Math.min(values.size(), first + pageSize)));
    }
}
