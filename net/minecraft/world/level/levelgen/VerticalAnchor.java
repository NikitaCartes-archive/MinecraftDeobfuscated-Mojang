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

public abstract class VerticalAnchor {
    public static final Codec<VerticalAnchor> CODEC = ExtraCodecs.xor(Absolute.CODEC, ExtraCodecs.xor(AboveBottom.CODEC, BelowTop.CODEC)).xmap(VerticalAnchor::merge, VerticalAnchor::split);
    private static final VerticalAnchor BOTTOM = VerticalAnchor.aboveBottom(0);
    private static final VerticalAnchor TOP = VerticalAnchor.belowTop(0);
    private final int value;

    protected VerticalAnchor(int i) {
        this.value = i;
    }

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
        return either2.map(Function.identity(), either -> (VerticalAnchor)either.map(Function.identity(), Function.identity()));
    }

    private static Either<Absolute, Either<AboveBottom, BelowTop>> split(VerticalAnchor verticalAnchor) {
        if (verticalAnchor instanceof Absolute) {
            return Either.left((Absolute)verticalAnchor);
        }
        return Either.right(verticalAnchor instanceof AboveBottom ? Either.left((AboveBottom)verticalAnchor) : Either.right((BelowTop)verticalAnchor));
    }

    protected int value() {
        return this.value;
    }

    public abstract int resolveY(WorldGenerationContext var1);

    static final class BelowTop
    extends VerticalAnchor {
        public static final Codec<BelowTop> CODEC = ((MapCodec)Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y).fieldOf("below_top")).xmap(BelowTop::new, VerticalAnchor::value).codec();

        protected BelowTop(int i) {
            super(i);
        }

        @Override
        public int resolveY(WorldGenerationContext worldGenerationContext) {
            return worldGenerationContext.getGenDepth() - 1 + worldGenerationContext.getMinGenY() - this.value();
        }

        public String toString() {
            return this.value() + " below top";
        }
    }

    static final class AboveBottom
    extends VerticalAnchor {
        public static final Codec<AboveBottom> CODEC = ((MapCodec)Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y).fieldOf("above_bottom")).xmap(AboveBottom::new, VerticalAnchor::value).codec();

        protected AboveBottom(int i) {
            super(i);
        }

        @Override
        public int resolveY(WorldGenerationContext worldGenerationContext) {
            return worldGenerationContext.getMinGenY() + this.value();
        }

        public String toString() {
            return this.value() + " above bottom";
        }
    }

    static final class Absolute
    extends VerticalAnchor {
        public static final Codec<Absolute> CODEC = ((MapCodec)Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y).fieldOf("absolute")).xmap(Absolute::new, VerticalAnchor::value).codec();

        protected Absolute(int i) {
            super(i);
        }

        @Override
        public int resolveY(WorldGenerationContext worldGenerationContext) {
            return this.value();
        }

        public String toString() {
            return this.value() + " absolute";
        }
    }
}

