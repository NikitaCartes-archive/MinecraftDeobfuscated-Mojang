package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseEntityBlock extends Block implements EntityBlock {
	protected BaseEntityBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int i, int j) {
		super.triggerEvent(blockState, level, blockPos, i, j);
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		return blockEntity == null ? false : blockEntity.triggerEvent(i, j);
	}

	@Nullable
	@Override
	public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		return blockEntity instanceof MenuProvider ? (MenuProvider)blockEntity : null;
	}
}
