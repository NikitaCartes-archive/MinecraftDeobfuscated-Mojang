/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world;

import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

public enum Difficulty {
    PEACEFUL(0, "peaceful"),
    EASY(1, "easy"),
    NORMAL(2, "normal"),
    HARD(3, "hard");

    private static final Difficulty[] BY_ID;
    private final int id;
    private final String key;

    private Difficulty(int j, String string2) {
        this.id = j;
        this.key = string2;
    }

    public int getId() {
        return this.id;
    }

    public Component getDisplayName() {
        return new TranslatableComponent("options.difficulty." + this.key, new Object[0]);
    }

    public static Difficulty byId(int i) {
        return BY_ID[i % BY_ID.length];
    }

    @Nullable
    public static Difficulty byName(String string) {
        for (Difficulty difficulty : Difficulty.values()) {
            if (!difficulty.key.equals(string)) continue;
            return difficulty;
        }
        return null;
    }

    public String getKey() {
        return this.key;
    }

    static {
        BY_ID = (Difficulty[])Arrays.stream(Difficulty.values()).sorted(Comparator.comparingInt(Difficulty::getId)).toArray(Difficulty[]::new);
    }
}

