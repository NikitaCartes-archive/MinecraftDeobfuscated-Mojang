/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.models.model;

import org.jetbrains.annotations.Nullable;

public final class TextureSlot {
    public static final TextureSlot ALL = TextureSlot.create("all");
    public static final TextureSlot TEXTURE = TextureSlot.create("texture", ALL);
    public static final TextureSlot PARTICLE = TextureSlot.create("particle", TEXTURE);
    public static final TextureSlot END = TextureSlot.create("end", ALL);
    public static final TextureSlot BOTTOM = TextureSlot.create("bottom", END);
    public static final TextureSlot TOP = TextureSlot.create("top", END);
    public static final TextureSlot FRONT = TextureSlot.create("front", ALL);
    public static final TextureSlot BACK = TextureSlot.create("back", ALL);
    public static final TextureSlot SIDE = TextureSlot.create("side", ALL);
    public static final TextureSlot NORTH = TextureSlot.create("north", SIDE);
    public static final TextureSlot SOUTH = TextureSlot.create("south", SIDE);
    public static final TextureSlot EAST = TextureSlot.create("east", SIDE);
    public static final TextureSlot WEST = TextureSlot.create("west", SIDE);
    public static final TextureSlot UP = TextureSlot.create("up");
    public static final TextureSlot DOWN = TextureSlot.create("down");
    public static final TextureSlot CROSS = TextureSlot.create("cross");
    public static final TextureSlot PLANT = TextureSlot.create("plant");
    public static final TextureSlot WALL = TextureSlot.create("wall", ALL);
    public static final TextureSlot RAIL = TextureSlot.create("rail");
    public static final TextureSlot WOOL = TextureSlot.create("wool");
    public static final TextureSlot PATTERN = TextureSlot.create("pattern");
    public static final TextureSlot PANE = TextureSlot.create("pane");
    public static final TextureSlot EDGE = TextureSlot.create("edge");
    public static final TextureSlot FAN = TextureSlot.create("fan");
    public static final TextureSlot STEM = TextureSlot.create("stem");
    public static final TextureSlot UPPER_STEM = TextureSlot.create("upperstem");
    public static final TextureSlot CROP = TextureSlot.create("crop");
    public static final TextureSlot DIRT = TextureSlot.create("dirt");
    public static final TextureSlot FIRE = TextureSlot.create("fire");
    public static final TextureSlot LANTERN = TextureSlot.create("lantern");
    public static final TextureSlot PLATFORM = TextureSlot.create("platform");
    public static final TextureSlot UNSTICKY = TextureSlot.create("unsticky");
    public static final TextureSlot TORCH = TextureSlot.create("torch");
    public static final TextureSlot LAYER0 = TextureSlot.create("layer0");
    public static final TextureSlot LIT_LOG = TextureSlot.create("lit_log");
    private final String id;
    @Nullable
    private final TextureSlot parent;

    private static TextureSlot create(String string) {
        return new TextureSlot(string, null);
    }

    private static TextureSlot create(String string, TextureSlot textureSlot) {
        return new TextureSlot(string, textureSlot);
    }

    private TextureSlot(String string, @Nullable TextureSlot textureSlot) {
        this.id = string;
        this.parent = textureSlot;
    }

    public String getId() {
        return this.id;
    }

    @Nullable
    public TextureSlot getParent() {
        return this.parent;
    }

    public String toString() {
        return "#" + this.id;
    }
}

