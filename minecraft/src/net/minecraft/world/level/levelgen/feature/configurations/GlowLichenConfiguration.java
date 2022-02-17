package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GlowLichenConfiguration implements FeatureConfiguration {
	public static final Codec<GlowLichenConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockState.CODEC.fieldOf("block_to_place").forGetter(glowLichenConfiguration -> glowLichenConfiguration.blockToPlace),
					Codec.intRange(1, 64).fieldOf("search_range").orElse(10).forGetter(glowLichenConfiguration -> glowLichenConfiguration.searchRange),
					Codec.BOOL.fieldOf("can_place_on_floor").orElse(false).forGetter(glowLichenConfiguration -> glowLichenConfiguration.canPlaceOnFloor),
					Codec.BOOL.fieldOf("can_place_on_ceiling").orElse(false).forGetter(glowLichenConfiguration -> glowLichenConfiguration.canPlaceOnCeiling),
					Codec.BOOL.fieldOf("can_place_on_wall").orElse(false).forGetter(glowLichenConfiguration -> glowLichenConfiguration.canPlaceOnWall),
					Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_spreading").orElse(0.5F).forGetter(glowLichenConfiguration -> glowLichenConfiguration.chanceOfSpreading),
					Registry.BLOCK.byNameCodec().listOf().fieldOf("can_be_placed_on").forGetter(glowLichenConfiguration -> glowLichenConfiguration.canBePlacedOn)
				)
				.apply(instance, GlowLichenConfiguration::new)
	);
	public final BlockState blockToPlace;
	public final int searchRange;
	public final boolean canPlaceOnFloor;
	public final boolean canPlaceOnCeiling;
	public final boolean canPlaceOnWall;
	public final float chanceOfSpreading;
	public final List<Block> canBePlacedOn;
	public final List<Direction> validDirections;

	public GlowLichenConfiguration(BlockState blockState, int i, boolean bl, boolean bl2, boolean bl3, float f, List<Block> list) {
		this.blockToPlace = blockState;
		this.searchRange = i;
		this.canPlaceOnFloor = bl;
		this.canPlaceOnCeiling = bl2;
		this.canPlaceOnWall = bl3;
		this.chanceOfSpreading = f;
		this.canBePlacedOn = list;
		List<Direction> list2 = Lists.<Direction>newArrayList();
		if (bl2) {
			list2.add(Direction.UP);
		}

		if (bl) {
			list2.add(Direction.DOWN);
		}

		if (bl3) {
			Direction.Plane.HORIZONTAL.forEach(list2::add);
		}

		this.validDirections = Collections.unmodifiableList(list2);
	}
}
