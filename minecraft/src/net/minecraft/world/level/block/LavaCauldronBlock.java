package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class LavaCauldronBlock extends AbstractCauldronBlock {
	public static final MapCodec<LavaCauldronBlock> CODEC = simpleCodec(LavaCauldronBlock::new);

	@Override
	public MapCodec<LavaCauldronBlock> codec() {
		return CODEC;
	}

	public LavaCauldronBlock(BlockBehaviour.Properties properties) {
		super(properties, CauldronInteraction.LAVA);
	}

	@Override
	protected double getContentHeight(BlockState blockState) {
		return 0.9375;
	}

	@Override
	public boolean isFull(BlockState blockState) {
		return true;
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (this.isEntityInsideContent(blockState, blockPos, entity)) {
			entity.lavaHurt();
		}
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return 3;
	}
}
