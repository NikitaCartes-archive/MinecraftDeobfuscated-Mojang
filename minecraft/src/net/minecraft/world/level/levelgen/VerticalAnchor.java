package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.dimension.DimensionType;

public interface VerticalAnchor {
	Codec<VerticalAnchor> CODEC = ExtraCodecs.xor(VerticalAnchor.Absolute.CODEC, ExtraCodecs.xor(VerticalAnchor.AboveBottom.CODEC, VerticalAnchor.BelowTop.CODEC))
		.xmap(VerticalAnchor::merge, VerticalAnchor::split);
	VerticalAnchor BOTTOM = aboveBottom(0);
	VerticalAnchor TOP = belowTop(0);

	static VerticalAnchor absolute(int i) {
		return new VerticalAnchor.Absolute(i);
	}

	static VerticalAnchor aboveBottom(int i) {
		return new VerticalAnchor.AboveBottom(i);
	}

	static VerticalAnchor belowTop(int i) {
		return new VerticalAnchor.BelowTop(i);
	}

	static VerticalAnchor bottom() {
		return BOTTOM;
	}

	static VerticalAnchor top() {
		return TOP;
	}

	private static VerticalAnchor merge(Either<VerticalAnchor.Absolute, Either<VerticalAnchor.AboveBottom, VerticalAnchor.BelowTop>> either) {
		return either.map(Function.identity(), eitherx -> eitherx.map(Function.identity(), Function.identity()));
	}

	private static Either<VerticalAnchor.Absolute, Either<VerticalAnchor.AboveBottom, VerticalAnchor.BelowTop>> split(VerticalAnchor verticalAnchor) {
		return verticalAnchor instanceof VerticalAnchor.Absolute
			? Either.left((VerticalAnchor.Absolute)verticalAnchor)
			: Either.right(
				verticalAnchor instanceof VerticalAnchor.AboveBottom
					? Either.left((VerticalAnchor.AboveBottom)verticalAnchor)
					: Either.right((VerticalAnchor.BelowTop)verticalAnchor)
			);
	}

	int resolveY(WorldGenerationContext worldGenerationContext);

	public static record AboveBottom(int offset) implements VerticalAnchor {
		public static final Codec<VerticalAnchor.AboveBottom> CODEC = Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y)
			.fieldOf("above_bottom")
			.<VerticalAnchor.AboveBottom>xmap(VerticalAnchor.AboveBottom::new, VerticalAnchor.AboveBottom::offset)
			.codec();

		@Override
		public int resolveY(WorldGenerationContext worldGenerationContext) {
			return worldGenerationContext.getMinGenY() + this.offset;
		}

		public String toString() {
			return this.offset + " above bottom";
		}
	}

	public static record Absolute(int y) implements VerticalAnchor {
		public static final Codec<VerticalAnchor.Absolute> CODEC = Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y)
			.fieldOf("absolute")
			.<VerticalAnchor.Absolute>xmap(VerticalAnchor.Absolute::new, VerticalAnchor.Absolute::y)
			.codec();

		@Override
		public int resolveY(WorldGenerationContext worldGenerationContext) {
			return this.y;
		}

		public String toString() {
			return this.y + " absolute";
		}
	}

	public static record BelowTop(int offset) implements VerticalAnchor {
		public static final Codec<VerticalAnchor.BelowTop> CODEC = Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y)
			.fieldOf("below_top")
			.<VerticalAnchor.BelowTop>xmap(VerticalAnchor.BelowTop::new, VerticalAnchor.BelowTop::offset)
			.codec();

		@Override
		public int resolveY(WorldGenerationContext worldGenerationContext) {
			return worldGenerationContext.getGenDepth() - 1 + worldGenerationContext.getMinGenY() - this.offset;
		}

		public String toString() {
			return this.offset + " below top";
		}
	}
}
