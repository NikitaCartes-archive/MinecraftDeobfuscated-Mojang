/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world;

import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

public enum Difficulty implements StringRepresentable
{
    PEACEFUL(0, "peaceful"),
    EASY(1, "easy"),
    NORMAL(2, "normal"),
    HARD(3, "hard");

    public static final StringRepresentable.EnumCodec<Difficulty> CODEC;
    private static final IntFunction<Difficulty> BY_ID;
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
        return Component.translatable("options.difficulty." + this.key);
    }

    public Component getInfo() {
        return Component.translatable("options.difficulty." + this.key + ".info");
    }

    public static Difficulty byId(int i) {
        return BY_ID.apply(i);
    }

    @Nullable
    public static Difficulty byName(String string) {
        return CODEC.byName(string);
    }

    public String getKey() {
        return this.key;
    }

    @Override
    public String getSerializedName() {
        return this.key;
    }

    static {
        CODEC = StringRepresentable.fromEnum(Difficulty::values);
        BY_ID = ByIdMap.continuous(Difficulty::getId, Difficulty.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    }
}

