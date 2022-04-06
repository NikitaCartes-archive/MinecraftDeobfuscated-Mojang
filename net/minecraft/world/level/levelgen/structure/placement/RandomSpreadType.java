/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

public enum RandomSpreadType implements StringRepresentable
{
    LINEAR("linear"),
    TRIANGULAR("triangular");

    public static final Codec<RandomSpreadType> CODEC;
    private final String id;

    private RandomSpreadType(String string2) {
        this.id = string2;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    public int evaluate(RandomSource randomSource, int i) {
        return switch (this) {
            default -> throw new IncompatibleClassChangeError();
            case LINEAR -> randomSource.nextInt(i);
            case TRIANGULAR -> (randomSource.nextInt(i) + randomSource.nextInt(i)) / 2;
        };
    }

    static {
        CODEC = StringRepresentable.fromEnum(RandomSpreadType::values);
    }
}

