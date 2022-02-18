/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public interface VerticalAnchor {
    public static final Codec<VerticalAnchor> CODEC = ExtraCodecs.xor(Absolute.CODEC, ExtraCodecs.xor(AboveBottom.CODEC, BelowTop.CODEC)).xmap(VerticalAnchor::merge, VerticalAnchor::split);
    public static final VerticalAnchor BOTTOM = VerticalAnchor.aboveBottom(0);
    public static final VerticalAnchor TOP = VerticalAnchor.belowTop(0);

    public static VerticalAnchor absolute(int i) {
        return new Absolute(i);
    }

    public static VerticalAnchor aboveBottom(int i) {
        return new AboveBottom(i);
    }

    public static VerticalAnchor belowTop(int i) {
        return new BelowTop(i);
    }

    public static VerticalAnchor bottom() {
        return BOTTOM;
    }

    public static VerticalAnchor top() {
        return TOP;
    }

    private static VerticalAnchor merge(Either<Absolute, Either<AboveBottom, BelowTop>> either2) {
        return (VerticalAnchor)((Object)either2.map(Function.identity(), either -> (Record)either.map(Function.identity(), Function.identity())));
    }

    private static Either<Absolute, Either<AboveBottom, BelowTop>> split(VerticalAnchor verticalAnchor) {
        if (verticalAnchor instanceof Absolute) {
            return Either.left((Absolute)verticalAnchor);
        }
        return Either.right(verticalAnchor instanceof AboveBottom ? Either.left((AboveBottom)verticalAnchor) : Either.right((BelowTop)verticalAnchor));
    }

    public int resolveY(WorldGenerationContext var1);

    public record Absolute(int y) implements VerticalAnchor
    {
        public static final Codec<Absolute> CODEC = ((MapCodec)Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y).fieldOf("absolute")).xmap(Absolute::new, Absolute::y).codec();

        @Override
        public int resolveY(WorldGenerationContext worldGenerationContext) {
            return this.y;
        }

        @Override
        public String toString() {
            return this.y + " absolute";
        }
    }

    public record AboveBottom(int offset) implements VerticalAnchor
    {
        public static final Codec<AboveBottom> CODEC = ((MapCodec)Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y).fieldOf("above_bottom")).xmap(AboveBottom::new, AboveBottom::offset).codec();

        @Override
        public int resolveY(WorldGenerationContext worldGenerationContext) {
            return worldGenerationContext.getMinGenY() + this.offset;
        }

        @Override
        public String toString() {
            return this.offset + " above bottom";
        }
    }

    public record BelowTop(int offset) implements VerticalAnchor
    {
        public static final Codec<BelowTop> CODEC = ((MapCodec)Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y).fieldOf("below_top")).xmap(BelowTop::new, BelowTop::offset).codec();

        @Override
        public int resolveY(WorldGenerationContext worldGenerationContext) {
            return worldGenerationContext.getGenDepth() - 1 + worldGenerationContext.getMinGenY() - this.offset;
        }

        @Override
        public String toString() {
            return this.offset + " below top";
        }
    }
}

