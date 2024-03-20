package net.minecraft.world.level.saveddata.maps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BannerBlockEntity;

public record MapBanner(BlockPos pos, DyeColor color, Optional<Component> name) {
	public static final Codec<MapBanner> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockPos.CODEC.fieldOf("pos").forGetter(MapBanner::pos),
					DyeColor.CODEC.optionalFieldOf("color", DyeColor.WHITE).forGetter(MapBanner::color),
					ComponentSerialization.FLAT_CODEC.optionalFieldOf("name").forGetter(MapBanner::name)
				)
				.apply(instance, MapBanner::new)
	);
	public static final Codec<List<MapBanner>> LIST_CODEC = CODEC.listOf();

	@Nullable
	public static MapBanner fromWorld(BlockGetter blockGetter, BlockPos blockPos) {
		if (blockGetter.getBlockEntity(blockPos) instanceof BannerBlockEntity bannerBlockEntity) {
			DyeColor dyeColor = bannerBlockEntity.getBaseColor();
			Optional<Component> optional = Optional.ofNullable(bannerBlockEntity.getCustomName());
			return new MapBanner(blockPos, dyeColor, optional);
		} else {
			return null;
		}
	}

	public Holder<MapDecorationType> getDecoration() {
		return switch (this.color) {
			case WHITE -> MapDecorationTypes.WHITE_BANNER;
			case ORANGE -> MapDecorationTypes.ORANGE_BANNER;
			case MAGENTA -> MapDecorationTypes.MAGENTA_BANNER;
			case LIGHT_BLUE -> MapDecorationTypes.LIGHT_BLUE_BANNER;
			case YELLOW -> MapDecorationTypes.YELLOW_BANNER;
			case LIME -> MapDecorationTypes.LIME_BANNER;
			case PINK -> MapDecorationTypes.PINK_BANNER;
			case GRAY -> MapDecorationTypes.GRAY_BANNER;
			case LIGHT_GRAY -> MapDecorationTypes.LIGHT_GRAY_BANNER;
			case CYAN -> MapDecorationTypes.CYAN_BANNER;
			case PURPLE -> MapDecorationTypes.PURPLE_BANNER;
			case BLUE -> MapDecorationTypes.BLUE_BANNER;
			case BROWN -> MapDecorationTypes.BROWN_BANNER;
			case GREEN -> MapDecorationTypes.GREEN_BANNER;
			case RED -> MapDecorationTypes.RED_BANNER;
			case BLACK -> MapDecorationTypes.BLACK_BANNER;
		};
	}

	public String getId() {
		return "banner-" + this.pos.getX() + "," + this.pos.getY() + "," + this.pos.getZ();
	}
}
