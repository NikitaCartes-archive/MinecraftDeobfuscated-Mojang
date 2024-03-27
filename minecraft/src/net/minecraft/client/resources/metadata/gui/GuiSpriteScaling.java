package net.minecraft.client.resources.metadata.gui;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

@Environment(EnvType.CLIENT)
public interface GuiSpriteScaling {
	Codec<GuiSpriteScaling> CODEC = GuiSpriteScaling.Type.CODEC.dispatch(GuiSpriteScaling::type, GuiSpriteScaling.Type::codec);
	GuiSpriteScaling DEFAULT = new GuiSpriteScaling.Stretch();

	GuiSpriteScaling.Type type();

	@Environment(EnvType.CLIENT)
	public static record NineSlice(int width, int height, GuiSpriteScaling.NineSlice.Border border) implements GuiSpriteScaling {
		public static final MapCodec<GuiSpriteScaling.NineSlice> CODEC = RecordCodecBuilder.<GuiSpriteScaling.NineSlice>mapCodec(
				instance -> instance.group(
							ExtraCodecs.POSITIVE_INT.fieldOf("width").forGetter(GuiSpriteScaling.NineSlice::width),
							ExtraCodecs.POSITIVE_INT.fieldOf("height").forGetter(GuiSpriteScaling.NineSlice::height),
							GuiSpriteScaling.NineSlice.Border.CODEC.fieldOf("border").forGetter(GuiSpriteScaling.NineSlice::border)
						)
						.apply(instance, GuiSpriteScaling.NineSlice::new)
			)
			.validate(GuiSpriteScaling.NineSlice::validate);

		private static DataResult<GuiSpriteScaling.NineSlice> validate(GuiSpriteScaling.NineSlice nineSlice) {
			GuiSpriteScaling.NineSlice.Border border = nineSlice.border();
			if (border.left() + border.right() >= nineSlice.width()) {
				return DataResult.error(() -> "Nine-sliced texture has no horizontal center slice: " + border.left() + " + " + border.right() + " >= " + nineSlice.width());
			} else {
				return border.top() + border.bottom() >= nineSlice.height()
					? DataResult.error(() -> "Nine-sliced texture has no vertical center slice: " + border.top() + " + " + border.bottom() + " >= " + nineSlice.height())
					: DataResult.success(nineSlice);
			}
		}

		@Override
		public GuiSpriteScaling.Type type() {
			return GuiSpriteScaling.Type.NINE_SLICE;
		}

		@Environment(EnvType.CLIENT)
		public static record Border(int left, int top, int right, int bottom) {
			private static final Codec<GuiSpriteScaling.NineSlice.Border> VALUE_CODEC = ExtraCodecs.POSITIVE_INT
				.flatComapMap(integer -> new GuiSpriteScaling.NineSlice.Border(integer, integer, integer, integer), border -> {
					OptionalInt optionalInt = border.unpackValue();
					return optionalInt.isPresent() ? DataResult.success(optionalInt.getAsInt()) : DataResult.error(() -> "Border has different side sizes");
				});
			private static final Codec<GuiSpriteScaling.NineSlice.Border> RECORD_CODEC = RecordCodecBuilder.create(
				instance -> instance.group(
							ExtraCodecs.NON_NEGATIVE_INT.fieldOf("left").forGetter(GuiSpriteScaling.NineSlice.Border::left),
							ExtraCodecs.NON_NEGATIVE_INT.fieldOf("top").forGetter(GuiSpriteScaling.NineSlice.Border::top),
							ExtraCodecs.NON_NEGATIVE_INT.fieldOf("right").forGetter(GuiSpriteScaling.NineSlice.Border::right),
							ExtraCodecs.NON_NEGATIVE_INT.fieldOf("bottom").forGetter(GuiSpriteScaling.NineSlice.Border::bottom)
						)
						.apply(instance, GuiSpriteScaling.NineSlice.Border::new)
			);
			static final Codec<GuiSpriteScaling.NineSlice.Border> CODEC = Codec.either(VALUE_CODEC, RECORD_CODEC)
				.xmap(Either::unwrap, border -> border.unpackValue().isPresent() ? Either.left(border) : Either.right(border));

			private OptionalInt unpackValue() {
				return this.left() == this.top() && this.top() == this.right() && this.right() == this.bottom() ? OptionalInt.of(this.left()) : OptionalInt.empty();
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static record Stretch() implements GuiSpriteScaling {
		public static final MapCodec<GuiSpriteScaling.Stretch> CODEC = MapCodec.unit(GuiSpriteScaling.Stretch::new);

		@Override
		public GuiSpriteScaling.Type type() {
			return GuiSpriteScaling.Type.STRETCH;
		}
	}

	@Environment(EnvType.CLIENT)
	public static record Tile(int width, int height) implements GuiSpriteScaling {
		public static final MapCodec<GuiSpriteScaling.Tile> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						ExtraCodecs.POSITIVE_INT.fieldOf("width").forGetter(GuiSpriteScaling.Tile::width),
						ExtraCodecs.POSITIVE_INT.fieldOf("height").forGetter(GuiSpriteScaling.Tile::height)
					)
					.apply(instance, GuiSpriteScaling.Tile::new)
		);

		@Override
		public GuiSpriteScaling.Type type() {
			return GuiSpriteScaling.Type.TILE;
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum Type implements StringRepresentable {
		STRETCH("stretch", GuiSpriteScaling.Stretch.CODEC),
		TILE("tile", GuiSpriteScaling.Tile.CODEC),
		NINE_SLICE("nine_slice", GuiSpriteScaling.NineSlice.CODEC);

		public static final Codec<GuiSpriteScaling.Type> CODEC = StringRepresentable.fromEnum(GuiSpriteScaling.Type::values);
		private final String key;
		private final MapCodec<? extends GuiSpriteScaling> codec;

		private Type(String string2, MapCodec<? extends GuiSpriteScaling> mapCodec) {
			this.key = string2;
			this.codec = mapCodec;
		}

		@Override
		public String getSerializedName() {
			return this.key;
		}

		public MapCodec<? extends GuiSpriteScaling> codec() {
			return this.codec;
		}
	}
}
