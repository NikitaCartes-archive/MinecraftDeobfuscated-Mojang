package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class LunarBaseFeature extends Feature<NoneFeatureConfiguration> {
	public LunarBaseFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = worldGenLevel.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, featurePlaceContext.origin());
		this.setBlock(worldGenLevel, blockPos, Blocks.IRON_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF, Half.TOP));
		BlockState blockState = Blocks.POLISHED_BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
		BlockState blockState2 = Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
		this.setBlock(worldGenLevel, blockPos.north().east(), blockState);
		this.setBlock(worldGenLevel, blockPos.north(), blockState2);
		this.setBlock(worldGenLevel, blockPos.north().west(), blockState);
		this.setBlock(worldGenLevel, blockPos.south().east(), blockState);
		this.setBlock(worldGenLevel, blockPos.south(), blockState2);
		this.setBlock(worldGenLevel, blockPos.south().west(), blockState);
		this.setBlock(worldGenLevel, blockPos.above(), Blocks.DROPPER.defaultBlockState());
		this.setBlock(worldGenLevel, blockPos.above().above(), Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE.defaultBlockState());
		this.setBlock(worldGenLevel, blockPos.above().north(), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState());
		this.setBlock(worldGenLevel, blockPos.above().south(), Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState());
		this.setBlock(worldGenLevel, blockPos.above().east(), Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, Boolean.valueOf(true)));
		this.setBlock(worldGenLevel, blockPos.above().west(), Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, Boolean.valueOf(true)));
		this.setBlock(worldGenLevel, blockPos.above().east().above(), Blocks.END_ROD.defaultBlockState());
		this.setBlock(worldGenLevel, blockPos.above().west().above(), Blocks.LIGHTNING_ROD.defaultBlockState().setValue(LightningRodBlock.FACING, Direction.DOWN));
		if (worldGenLevel.getBlockEntity(blockPos.above()) instanceof DropperBlockEntity dropperBlockEntity) {
			dropperBlockEntity.setLunar();
			dropperBlockEntity.setCustomName(Component.translatable("block.minecraft.dropper.lunar"));
		}

		return true;
	}
}
