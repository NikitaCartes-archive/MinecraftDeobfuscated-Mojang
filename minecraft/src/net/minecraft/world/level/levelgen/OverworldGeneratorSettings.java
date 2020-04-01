package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class OverworldGeneratorSettings extends ChunkGeneratorSettings {
	private final int biomeSize;
	private final int riverSize;
	private final int fixedBiome = -1;
	private final int seaLevel;
	public static final List<BlockState> SAFE_BLOCKS = (List<BlockState>)Registry.BLOCK
		.stream()
		.filter(block -> !block.isUnstable() && !block.isEntityBlock())
		.map(Block::defaultBlockState)
		.filter(blockState -> !PoiType.isPoi(blockState))
		.collect(ImmutableList.toImmutableList());
	public static final List<BlockState> GROUND_BLOCKS = (List<BlockState>)Registry.BLOCK
		.stream()
		.filter(block -> !block.isUnstable() && !block.isEntityBlock() && !block.hasDynamicShape())
		.map(Block::defaultBlockState)
		.filter(blockState -> !PoiType.isPoi(blockState) && blockState.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO))
		.collect(ImmutableList.toImmutableList());

	public OverworldGeneratorSettings() {
		this.seaLevel = 63;
		this.biomeSize = 4;
		this.riverSize = 4;
	}

	public OverworldGeneratorSettings(Random random) {
		this.seaLevel = random.nextInt(128);
		this.biomeSize = random.nextInt(8) + 1;
		this.riverSize = random.nextInt(8) + 1;
		this.defaultBlock = this.randomGroundBlock(random);
		this.defaultFluid = this.randomLiquidBlock(random);
	}

	public int getBiomeSize() {
		return this.biomeSize;
	}

	public int getRiverSize() {
		return this.riverSize;
	}

	public int getFixedBiome() {
		return -1;
	}

	@Override
	public int getBedrockFloorPosition() {
		return 0;
	}
}
