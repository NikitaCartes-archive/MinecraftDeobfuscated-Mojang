package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.dimension.DimensionType;

public abstract class VerticalAnchor {
	public static final Codec<VerticalAnchor> CODEC = ExtraCodecs.xor(
			VerticalAnchor.Absolute.CODEC, ExtraCodecs.xor(VerticalAnchor.AboveBottom.CODEC, VerticalAnchor.BelowTop.CODEC)
		)
		.xmap(VerticalAnchor::merge, VerticalAnchor::split);
	private static final VerticalAnchor BOTTOM = aboveBottom(0);
	private static final VerticalAnchor TOP = belowTop(0);
	private final int value;

	protected VerticalAnchor(int i) {
		this.value = i;
	}

	public static VerticalAnchor absolute(int i) {
		return new VerticalAnchor.Absolute(i);
	}

	public static VerticalAnchor aboveBottom(int i) {
		return new VerticalAnchor.AboveBottom(i);
	}

	public static VerticalAnchor belowTop(int i) {
		return new VerticalAnchor.BelowTop(i);
	}

	public static VerticalAnchor bottom() {
		return BOTTOM;
	}

	public static VerticalAnchor top() {
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

	protected int value() {
		return this.value;
	}

	public abstract int resolveY(WorldGenerationContext worldGenerationContext);

	static final class AboveBottom extends VerticalAnchor {
		public static final Codec<VerticalAnchor.AboveBottom> CODEC = Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y)
			.fieldOf("above_bottom")
			.<VerticalAnchor.AboveBottom>xmap(VerticalAnchor.AboveBottom::new, VerticalAnchor::value)
			.codec();

		protected AboveBottom(int i) {
			super(i);
		}

		@Override
		public int resolveY(WorldGenerationContext worldGenerationContext) {
			return worldGenerationContext.getMinGenY() + this.value();
		}
	}

	static final class Absolute extends VerticalAnchor {
		public static final Codec<VerticalAnchor.Absolute> CODEC = Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y)
			.fieldOf("absolute")
			.<VerticalAnchor.Absolute>xmap(VerticalAnchor.Absolute::new, VerticalAnchor::value)
			.codec();

		protected Absolute(int i) {
			super(i);
		}

		@Override
		public int resolveY(WorldGenerationContext worldGenerationContext) {
			return this.value();
		}
	}

	static final class BelowTop extends VerticalAnchor {
		public static final Codec<VerticalAnchor.BelowTop> CODEC = Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y)
			.fieldOf("below_top")
			.<VerticalAnchor.BelowTop>xmap(VerticalAnchor.BelowTop::new, VerticalAnchor::value)
			.codec();

		protected BelowTop(int i) {
			super(i);
		}

		@Override
		public int resolveY(WorldGenerationContext worldGenerationContext) {
			return worldGenerationContext.getGenDepth() - 1 + worldGenerationContext.getMinGenY() - this.value();
		}
	}
}
