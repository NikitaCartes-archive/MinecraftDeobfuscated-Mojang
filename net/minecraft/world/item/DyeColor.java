/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public enum DyeColor implements StringRepresentable
{
    WHITE(0, "white", 0xF9FFFE, MaterialColor.SNOW, 0xF0F0F0, 0xFFFFFF),
    ORANGE(1, "orange", 16351261, MaterialColor.COLOR_ORANGE, 15435844, 16738335),
    MAGENTA(2, "magenta", 13061821, MaterialColor.COLOR_MAGENTA, 12801229, 0xFF00FF),
    LIGHT_BLUE(3, "light_blue", 3847130, MaterialColor.COLOR_LIGHT_BLUE, 6719955, 10141901),
    YELLOW(4, "yellow", 16701501, MaterialColor.COLOR_YELLOW, 14602026, 0xFFFF00),
    LIME(5, "lime", 8439583, MaterialColor.COLOR_LIGHT_GREEN, 4312372, 0xBFFF00),
    PINK(6, "pink", 15961002, MaterialColor.COLOR_PINK, 14188952, 16738740),
    GRAY(7, "gray", 4673362, MaterialColor.COLOR_GRAY, 0x434343, 0x808080),
    LIGHT_GRAY(8, "light_gray", 0x9D9D97, MaterialColor.COLOR_LIGHT_GRAY, 0xABABAB, 0xD3D3D3),
    CYAN(9, "cyan", 1481884, MaterialColor.COLOR_CYAN, 2651799, 65535),
    PURPLE(10, "purple", 8991416, MaterialColor.COLOR_PURPLE, 8073150, 10494192),
    BLUE(11, "blue", 3949738, MaterialColor.COLOR_BLUE, 2437522, 255),
    BROWN(12, "brown", 8606770, MaterialColor.COLOR_BROWN, 5320730, 9127187),
    GREEN(13, "green", 6192150, MaterialColor.COLOR_GREEN, 3887386, 65280),
    RED(14, "red", 11546150, MaterialColor.COLOR_RED, 11743532, 0xFF0000),
    BLACK(15, "black", 0x1D1D21, MaterialColor.COLOR_BLACK, 0x1E1B1B, 0);

    private static final DyeColor[] BY_ID;
    private static final Int2ObjectOpenHashMap<DyeColor> BY_FIREWORK_COLOR;
    private final int id;
    private final String name;
    private final MaterialColor color;
    private final float[] textureDiffuseColors;
    private final int fireworkColor;
    private final int textColor;

    private DyeColor(int j, String string2, int k, MaterialColor materialColor, int l, int m) {
        this.id = j;
        this.name = string2;
        this.color = materialColor;
        this.textColor = m;
        int n = (k & 0xFF0000) >> 16;
        int o = (k & 0xFF00) >> 8;
        int p = (k & 0xFF) >> 0;
        this.textureDiffuseColors = new float[]{(float)n / 255.0f, (float)o / 255.0f, (float)p / 255.0f};
        this.fireworkColor = l;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public float[] getTextureDiffuseColors() {
        return this.textureDiffuseColors;
    }

    public MaterialColor getMaterialColor() {
        return this.color;
    }

    public int getFireworkColor() {
        return this.fireworkColor;
    }

    public int getTextColor() {
        return this.textColor;
    }

    public static DyeColor byId(int i) {
        if (i < 0 || i >= BY_ID.length) {
            i = 0;
        }
        return BY_ID[i];
    }

    @Nullable
    @Contract(value="_,!null->!null;_,null->_")
    public static DyeColor byName(String string, @Nullable DyeColor dyeColor) {
        for (DyeColor dyeColor2 : DyeColor.values()) {
            if (!dyeColor2.name.equals(string)) continue;
            return dyeColor2;
        }
        return dyeColor;
    }

    @Nullable
    public static DyeColor byFireworkColor(int i) {
        return BY_FIREWORK_COLOR.get(i);
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    static {
        BY_ID = (DyeColor[])Arrays.stream(DyeColor.values()).sorted(Comparator.comparingInt(DyeColor::getId)).toArray(DyeColor[]::new);
        BY_FIREWORK_COLOR = new Int2ObjectOpenHashMap<DyeColor>(Arrays.stream(DyeColor.values()).collect(Collectors.toMap(dyeColor -> dyeColor.fireworkColor, dyeColor -> dyeColor)));
    }
}

