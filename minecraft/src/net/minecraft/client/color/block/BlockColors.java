package net.minecraft.client.color.block;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.IdMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;

@Environment(EnvType.CLIENT)
public class BlockColors {
	private static final int DEFAULT = -1;
	private final IdMapper<BlockColor> blockColors = new IdMapper<>(32);
	private final Map<Block, Set<Property<?>>> coloringStates = Maps.<Block, Set<Property<?>>>newHashMap();

	public static BlockColors createDefault() {
		BlockColors blockColors = new BlockColors();
		blockColors.register(
			(blockState, blockAndTintGetter, blockPos, i) -> blockAndTintGetter != null && blockPos != null
					? BiomeColors.getAverageGrassColor(blockAndTintGetter, blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER ? blockPos.below() : blockPos)
					: GrassColor.getDefaultColor(),
			Blocks.LARGE_FERN,
			Blocks.TALL_GRASS
		);
		blockColors.addColoringState(DoublePlantBlock.HALF, Blocks.LARGE_FERN, Blocks.TALL_GRASS);
		blockColors.register(
			(blockState, blockAndTintGetter, blockPos, i) -> blockAndTintGetter != null && blockPos != null
					? BiomeColors.getAverageGrassColor(blockAndTintGetter, blockPos)
					: GrassColor.getDefaultColor(),
			Blocks.GRASS_BLOCK,
			Blocks.FERN,
			Blocks.SHORT_GRASS,
			Blocks.POTTED_FERN
		);
		blockColors.register((blockState, blockAndTintGetter, blockPos, i) -> {
			if (i != 0) {
				return blockAndTintGetter != null && blockPos != null ? BiomeColors.getAverageGrassColor(blockAndTintGetter, blockPos) : GrassColor.getDefaultColor();
			} else {
				return -1;
			}
		}, Blocks.PINK_PETALS);
		blockColors.register((blockState, blockAndTintGetter, blockPos, i) -> FoliageColor.getEvergreenColor(), Blocks.SPRUCE_LEAVES);
		blockColors.register((blockState, blockAndTintGetter, blockPos, i) -> FoliageColor.getBirchColor(), Blocks.BIRCH_LEAVES);
		blockColors.register(
			(blockState, blockAndTintGetter, blockPos, i) -> blockAndTintGetter != null && blockPos != null
					? BiomeColors.getAverageFoliageColor(blockAndTintGetter, blockPos)
					: FoliageColor.getDefaultColor(),
			Blocks.OAK_LEAVES,
			Blocks.JUNGLE_LEAVES,
			Blocks.ACACIA_LEAVES,
			Blocks.DARK_OAK_LEAVES,
			Blocks.VINE,
			Blocks.MANGROVE_LEAVES
		);
		blockColors.register(
			(blockState, blockAndTintGetter, blockPos, i) -> blockAndTintGetter != null && blockPos != null
					? BiomeColors.getAverageWaterColor(blockAndTintGetter, blockPos)
					: -1,
			Blocks.WATER,
			Blocks.BUBBLE_COLUMN,
			Blocks.WATER_CAULDRON
		);
		blockColors.register(
			(blockState, blockAndTintGetter, blockPos, i) -> RedStoneWireBlock.getColorForPower((Integer)blockState.getValue(RedStoneWireBlock.POWER)),
			Blocks.REDSTONE_WIRE
		);
		blockColors.addColoringState(RedStoneWireBlock.POWER, Blocks.REDSTONE_WIRE);
		blockColors.register(
			(blockState, blockAndTintGetter, blockPos, i) -> blockAndTintGetter != null && blockPos != null
					? BiomeColors.getAverageGrassColor(blockAndTintGetter, blockPos)
					: -1,
			Blocks.SUGAR_CANE
		);
		blockColors.register((blockState, blockAndTintGetter, blockPos, i) -> -2046180, Blocks.ATTACHED_MELON_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
		blockColors.register((blockState, blockAndTintGetter, blockPos, i) -> {
			int j = (Integer)blockState.getValue(StemBlock.AGE);
			return FastColor.ARGB32.color(j * 32, 255 - j * 8, j * 4);
		}, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
		blockColors.addColoringState(StemBlock.AGE, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
		blockColors.register((blockState, blockAndTintGetter, blockPos, i) -> blockAndTintGetter != null && blockPos != null ? -14647248 : -9321636, Blocks.LILY_PAD);
		return blockColors;
	}

	public int getColor(BlockState blockState, Level level, BlockPos blockPos) {
		BlockColor blockColor = this.blockColors.byId(BuiltInRegistries.BLOCK.getId(blockState.getBlock()));
		if (blockColor != null) {
			return blockColor.getColor(blockState, null, null, 0);
		} else {
			MapColor mapColor = blockState.getMapColor(level, blockPos);
			return mapColor != null ? mapColor.col : -1;
		}
	}

	public int getColor(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int i) {
		BlockColor blockColor = this.blockColors.byId(BuiltInRegistries.BLOCK.getId(blockState.getBlock()));
		return blockColor == null ? -1 : blockColor.getColor(blockState, blockAndTintGetter, blockPos, i);
	}

	public void register(BlockColor blockColor, Block... blocks) {
		for (Block block : blocks) {
			this.blockColors.addMapping(blockColor, BuiltInRegistries.BLOCK.getId(block));
		}
	}

	private void addColoringStates(Set<Property<?>> set, Block... blocks) {
		for (Block block : blocks) {
			this.coloringStates.put(block, set);
		}
	}

	private void addColoringState(Property<?> property, Block... blocks) {
		this.addColoringStates(ImmutableSet.of(property), blocks);
	}

	public Set<Property<?>> getColoringProperties(Block block) {
		return (Set<Property<?>>)this.coloringStates.getOrDefault(block, ImmutableSet.of());
	}
}
