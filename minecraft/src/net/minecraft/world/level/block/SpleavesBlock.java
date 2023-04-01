package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.MoonCow;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SpleavesBlock extends Block {
	protected static final VoxelShape SHAPE = Block.box(1.0, 1.0, 1.0, 15.0, 15.0, 15.0);
	public static Property<Boolean> FALLING = BlockStateProperties.FALLING;

	public SpleavesBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FALLING, Boolean.valueOf(false)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FALLING);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		super.stepOn(level, blockPos, blockState, entity);
		if (!(entity instanceof MoonCow)) {
			if (!(Boolean)blockState.getValue(FALLING) && level instanceof ServerLevel serverLevel) {
				level.setBlock(blockPos, blockState.setValue(FALLING, Boolean.valueOf(true)), 2);
				level.scheduleTick(blockPos, this, 7);
				int i = level.random.nextInt(3) + 1;
				int j = level.random.nextInt(3) + i;
				serverLevel.playSound(null, blockPos, SoundEvents.COPPER_BREAK, SoundSource.BLOCKS, 0.25F, 1.2F);
				serverLevel.getServer().executeLater(i, () -> serverLevel.playSound(null, blockPos, SoundEvents.COPPER_BREAK, SoundSource.BLOCKS, 0.28F, 1.5F));
				serverLevel.getServer().executeLater(j, () -> serverLevel.playSound(null, blockPos, SoundEvents.COPPER_BREAK, SoundSource.BLOCKS, 0.39F, 1.8F));
			}
		}
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		super.tick(blockState, serverLevel, blockPos, randomSource);
		serverLevel.destroyBlock(blockPos, false);
	}
}
