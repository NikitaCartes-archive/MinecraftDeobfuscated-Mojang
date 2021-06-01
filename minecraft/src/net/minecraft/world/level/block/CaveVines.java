package net.minecraft.world.level.block;

import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CaveVines {
	VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
	BooleanProperty BERRIES = BlockStateProperties.BERRIES;

	static InteractionResult use(BlockState blockState, Level level, BlockPos blockPos) {
		if ((Boolean)blockState.getValue(BERRIES)) {
			Block.popResource(level, blockPos, new ItemStack(Items.GLOW_BERRIES, 1));
			float f = Mth.randomBetween(level.random, 0.8F, 1.2F);
			level.playSound(null, blockPos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, f);
			level.setBlock(blockPos, blockState.setValue(BERRIES, Boolean.valueOf(false)), 2);
			return InteractionResult.sidedSuccess(level.isClientSide);
		} else {
			return InteractionResult.PASS;
		}
	}

	static boolean hasGlowBerries(BlockState blockState) {
		return blockState.hasProperty(BERRIES) && (Boolean)blockState.getValue(BERRIES);
	}

	static ToIntFunction<BlockState> emission(int i) {
		return blockState -> blockState.getValue(BlockStateProperties.BERRIES) ? i : 0;
	}
}
