package net.minecraft.world.level.saveddata.maps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BannerBlockEntity;

public record MapBanner(BlockPos pos, DyeColor color, Optional<Component> name) {
	public static final Codec<MapBanner> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockPos.CODEC.fieldOf("Pos").forGetter(MapBanner::pos),
					DyeColor.CODEC.optionalFieldOf("Color", DyeColor.WHITE).forGetter(MapBanner::color),
					ComponentSerialization.FLAT_CODEC.optionalFieldOf("Name").forGetter(MapBanner::name)
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

	public MapDecoration.Type getDecoration() {
		return switch (this.color) {
			case WHITE -> MapDecoration.Type.BANNER_WHITE;
			case ORANGE -> MapDecoration.Type.BANNER_ORANGE;
			case MAGENTA -> MapDecoration.Type.BANNER_MAGENTA;
			case LIGHT_BLUE -> MapDecoration.Type.BANNER_LIGHT_BLUE;
			case YELLOW -> MapDecoration.Type.BANNER_YELLOW;
			case LIME -> MapDecoration.Type.BANNER_LIME;
			case PINK -> MapDecoration.Type.BANNER_PINK;
			case GRAY -> MapDecoration.Type.BANNER_GRAY;
			case LIGHT_GRAY -> MapDecoration.Type.BANNER_LIGHT_GRAY;
			case CYAN -> MapDecoration.Type.BANNER_CYAN;
			case PURPLE -> MapDecoration.Type.BANNER_PURPLE;
			case BLUE -> MapDecoration.Type.BANNER_BLUE;
			case BROWN -> MapDecoration.Type.BANNER_BROWN;
			case GREEN -> MapDecoration.Type.BANNER_GREEN;
			case RED -> MapDecoration.Type.BANNER_RED;
			default -> MapDecoration.Type.BANNER_BLACK;
		};
	}

	public String getId() {
		return "banner-" + this.pos.getX() + "," + this.pos.getY() + "," + this.pos.getZ();
	}
}
