package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TurtleEggBlock extends Block {
	public static final int MAX_HATCH_LEVEL = 2;
	public static final int MIN_EGGS = 1;
	public static final int MAX_EGGS = 4;
	private static final VoxelShape ONE_EGG_AABB = Block.box(3.0, 0.0, 3.0, 12.0, 7.0, 12.0);
	private static final VoxelShape MULTIPLE_EGGS_AABB = Block.box(1.0, 0.0, 1.0, 15.0, 7.0, 15.0);
	public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
	public static final IntegerProperty EGGS = BlockStateProperties.EGGS;

	public TurtleEggBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, Integer.valueOf(0)).setValue(EGGS, Integer.valueOf(1)));
	}

	@Override
	public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
		if (!entity.isSteppingCarefully()) {
			this.destroyEgg(level, blockState, blockPos, entity, 100);
		}

		super.stepOn(level, blockPos, blockState, entity);
	}

	@Override
	public void fallOn(Level level, BlockState blockState, BlockPos blockPos, Entity entity, float f) {
		if (!(entity instanceof Zombie)) {
			this.destroyEgg(level, blockState, blockPos, entity, 3);
		}

		super.fallOn(level, blockState, blockPos, entity, f);
	}

	private void destroyEgg(Level level, BlockState blockState, BlockPos blockPos, Entity entity, int i) {
		if (this.canDestroyEgg(level, entity)) {
			if (!level.isClientSide && level.random.nextInt(i) == 0 && blockState.is(Blocks.TURTLE_EGG)) {
				this.decreaseEggs(level, blockPos, blockState);
			}
		}
	}

	private void decreaseEggs(Level level, BlockPos blockPos, BlockState blockState) {
		level.playSound(null, blockPos, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F + level.random.nextFloat() * 0.2F);
		int i = (Integer)blockState.getValue(EGGS);
		if (i <= 1) {
			level.destroyBlock(blockPos, false);
		} else {
			level.setBlock(blockPos, blockState.setValue(EGGS, Integer.valueOf(i - 1)), 2);
			level.levelEvent(2001, blockPos, Block.getId(blockState));
		}
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (this.shouldUpdateHatchLevel(serverLevel) && onSand(serverLevel, blockPos)) {
			int i = (Integer)blockState.getValue(HATCH);
			if (i < 2) {
				serverLevel.playSound(null, blockPos, SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 0.7F, 0.9F + random.nextFloat() * 0.2F);
				serverLevel.setBlock(blockPos, blockState.setValue(HATCH, Integer.valueOf(i + 1)), 2);
			} else {
				serverLevel.playSound(null, blockPos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.7F, 0.9F + random.nextFloat() * 0.2F);
				serverLevel.removeBlock(blockPos, false);

				for (int j = 0; j < blockState.getValue(EGGS); j++) {
					serverLevel.levelEvent(2001, blockPos, Block.getId(blockState));
					Turtle turtle = EntityType.TURTLE.create(serverLevel);
					turtle.setAge(-24000);
					turtle.setHomePos(blockPos);
					turtle.moveTo((double)blockPos.getX() + 0.3 + (double)j * 0.2, (double)blockPos.getY(), (double)blockPos.getZ() + 0.3, 0.0F, 0.0F);
					serverLevel.addFreshEntity(turtle);
				}
			}
		}
	}

	public static boolean onSand(BlockGetter blockGetter, BlockPos blockPos) {
		return isSand(blockGetter, blockPos.below());
	}

	public static boolean isSand(BlockGetter blockGetter, BlockPos blockPos) {
		return blockGetter.getBlockState(blockPos).is(BlockTags.SAND);
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (onSand(level, blockPos) && !level.isClientSide) {
			level.levelEvent(2005, blockPos, 0);
		}
	}

	private boolean shouldUpdateHatchLevel(Level level) {
		float f = level.getTimeOfDay(1.0F);
		return (double)f < 0.69 && (double)f > 0.65 ? true : level.random.nextInt(500) == 0;
	}

	@Override
	public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
		super.playerDestroy(level, player, blockPos, blockState, blockEntity, itemStack);
		this.decreaseEggs(level, blockPos, blockState);
	}

	@Override
	public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
		return !blockPlaceContext.isSecondaryUseActive() && blockPlaceContext.getItemInHand().is(this.asItem()) && blockState.getValue(EGGS) < 4
			? true
			: super.canBeReplaced(blockState, blockPlaceContext);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos());
		return blockState.is(this)
			? blockState.setValue(EGGS, Integer.valueOf(Math.min(4, (Integer)blockState.getValue(EGGS) + 1)))
			: super.getStateForPlacement(blockPlaceContext);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return blockState.getValue(EGGS) > 1 ? MULTIPLE_EGGS_AABB : ONE_EGG_AABB;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HATCH, EGGS);
	}

	private boolean canDestroyEgg(Level level, Entity entity) {
		if (entity instanceof Turtle || entity instanceof Bat) {
			return false;
		} else {
			return !(entity instanceof LivingEntity) ? false : entity instanceof Player || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
		}
	}
}
