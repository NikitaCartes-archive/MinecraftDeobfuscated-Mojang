package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WeepingVinesPlantBlock extends GrowingPlantBodyBlock {
	public static final MapCodec<WeepingVinesPlantBlock> CODEC = simpleCodec(WeepingVinesPlantBlock::new);
	public static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

	@Override
	public MapCodec<WeepingVinesPlantBlock> codec() {
		return CODEC;
	}

	public WeepingVinesPlantBlock(BlockBehaviour.Properties properties) {
		super(properties, Direction.DOWN, SHAPE, false);
	}

	@Override
	protected GrowingPlantHeadBlock getHeadBlock() {
		return (GrowingPlantHeadBlock)Blocks.WEEPING_VINES;
	}
}
