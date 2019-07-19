/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.decoration;

import net.minecraft.core.Registry;

public class Motive {
    public static final Motive KEBAB = Motive.register("kebab", 16, 16);
    public static final Motive AZTEC = Motive.register("aztec", 16, 16);
    public static final Motive ALBAN = Motive.register("alban", 16, 16);
    public static final Motive AZTEC2 = Motive.register("aztec2", 16, 16);
    public static final Motive BOMB = Motive.register("bomb", 16, 16);
    public static final Motive PLANT = Motive.register("plant", 16, 16);
    public static final Motive WASTELAND = Motive.register("wasteland", 16, 16);
    public static final Motive POOL = Motive.register("pool", 32, 16);
    public static final Motive COURBET = Motive.register("courbet", 32, 16);
    public static final Motive SEA = Motive.register("sea", 32, 16);
    public static final Motive SUNSET = Motive.register("sunset", 32, 16);
    public static final Motive CREEBET = Motive.register("creebet", 32, 16);
    public static final Motive WANDERER = Motive.register("wanderer", 16, 32);
    public static final Motive GRAHAM = Motive.register("graham", 16, 32);
    public static final Motive MATCH = Motive.register("match", 32, 32);
    public static final Motive BUST = Motive.register("bust", 32, 32);
    public static final Motive STAGE = Motive.register("stage", 32, 32);
    public static final Motive VOID = Motive.register("void", 32, 32);
    public static final Motive SKULL_AND_ROSES = Motive.register("skull_and_roses", 32, 32);
    public static final Motive WITHER = Motive.register("wither", 32, 32);
    public static final Motive FIGHTERS = Motive.register("fighters", 64, 32);
    public static final Motive POINTER = Motive.register("pointer", 64, 64);
    public static final Motive PIGSCENE = Motive.register("pigscene", 64, 64);
    public static final Motive BURNING_SKULL = Motive.register("burning_skull", 64, 64);
    public static final Motive SKELETON = Motive.register("skeleton", 64, 48);
    public static final Motive DONKEY_KONG = Motive.register("donkey_kong", 64, 48);
    private final int width;
    private final int height;

    private static Motive register(String string, int i, int j) {
        return Registry.register(Registry.MOTIVE, string, new Motive(i, j));
    }

    public Motive(int i, int j) {
        this.width = i;
        this.height = j;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}

