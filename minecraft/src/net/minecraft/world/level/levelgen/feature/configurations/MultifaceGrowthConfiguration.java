package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;

public class MultifaceGrowthConfiguration implements FeatureConfiguration {
	public static final Codec<MultifaceGrowthConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BuiltInRegistries.BLOCK
						.byNameCodec()
						.fieldOf("block")
						.<Block>flatXmap(MultifaceGrowthConfiguration::apply, DataResult::success)
						.orElse((MultifaceBlock)Blocks.GLOW_LICHEN)
						.forGetter(multifaceGrowthConfiguration -> multifaceGrowthConfiguration.placeBlock),
					Codec.intRange(1, 64).fieldOf("search_range").orElse(10).forGetter(multifaceGrowthConfiguration -> multifaceGrowthConfiguration.searchRange),
					Codec.BOOL.fieldOf("can_place_on_floor").orElse(false).forGetter(multifaceGrowthConfiguration -> multifaceGrowthConfiguration.canPlaceOnFloor),
					Codec.BOOL.fieldOf("can_place_on_ceiling").orElse(false).forGetter(multifaceGrowthConfiguration -> multifaceGrowthConfiguration.canPlaceOnCeiling),
					Codec.BOOL.fieldOf("can_place_on_wall").orElse(false).forGetter(multifaceGrowthConfiguration -> multifaceGrowthConfiguration.canPlaceOnWall),
					Codec.floatRange(0.0F, 1.0F)
						.fieldOf("chance_of_spreading")
						.orElse(0.5F)
						.forGetter(multifaceGrowthConfiguration -> multifaceGrowthConfiguration.chanceOfSpreading),
					RegistryCodecs.homogeneousList(Registries.BLOCK)
						.fieldOf("can_be_placed_on")
						.forGetter(multifaceGrowthConfiguration -> multifaceGrowthConfiguration.canBePlacedOn)
				)
				.apply(instance, MultifaceGrowthConfiguration::new)
	);
	public final MultifaceBlock placeBlock;
	public final int searchRange;
	public final boolean canPlaceOnFloor;
	public final boolean canPlaceOnCeiling;
	public final boolean canPlaceOnWall;
	public final float chanceOfSpreading;
	public final HolderSet<Block> canBePlacedOn;
	private final ObjectArrayList<Direction> validDirections;

	private static DataResult<MultifaceBlock> apply(Block block) {
		return block instanceof MultifaceBlock multifaceBlock
			? DataResult.success(multifaceBlock)
			: DataResult.error(() -> "Growth block should be a multiface block");
	}

	public MultifaceGrowthConfiguration(MultifaceBlock multifaceBlock, int i, boolean bl, boolean bl2, boolean bl3, float f, HolderSet<Block> holderSet) {
		this.placeBlock = multifaceBlock;
		this.searchRange = i;
		this.canPlaceOnFloor = bl;
		this.canPlaceOnCeiling = bl2;
		this.canPlaceOnWall = bl3;
		this.chanceOfSpreading = f;
		this.canBePlacedOn = holderSet;
		this.validDirections = new ObjectArrayList<>(6);
		if (bl2) {
			this.validDirections.add(Direction.UP);
		}

		if (bl) {
			this.validDirections.add(Direction.DOWN);
		}

		if (bl3) {
			Direction.Plane.HORIZONTAL.forEach(this.validDirections::add);
		}
	}

	public List<Direction> getShuffledDirectionsExcept(RandomSource randomSource, Direction direction) {
		return Util.toShuffledList(this.validDirections.stream().filter(direction2 -> direction2 != direction), randomSource);
	}

	public List<Direction> getShuffledDirections(RandomSource randomSource) {
		return Util.shuffledCopy(this.validDirections, randomSource);
	}
}
