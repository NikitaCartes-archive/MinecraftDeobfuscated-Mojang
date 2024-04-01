package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MashedPotatoBlock extends Block {
	public static final MapCodec<MashedPotatoBlock> CODEC = simpleCodec(MashedPotatoBlock::new);
	private static final float IN_BLOCK_HORIZONTAL_SPEED_MULTIPLIER = 0.9F;
	private static final float IN_BLOCK_VERTICAL_SPEED_MULTIPLIER = 1.5F;
	public static final int MAX_HEIGHT = 8;
	public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
	protected static final VoxelShape[] SHAPE_BY_LAYER = SnowLayerBlock.SHAPE_BY_LAYER;

	@Override
	protected MapCodec<MashedPotatoBlock> codec() {
		return CODEC;
	}

	protected MashedPotatoBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, Integer.valueOf(1)));
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		return true;
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE_BY_LAYER[blockState.getValue(LAYERS)];
	}

	@Override
	protected VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return SHAPE_BY_LAYER[blockState.getValue(LAYERS)];
	}

	@Override
	protected boolean useShapeForLightOcclusion(BlockState blockState) {
		return true;
	}

	@Override
	protected float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.getValue(LAYERS) == 8 ? 0.2F : 1.0F;
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockState blockState2 = levelReader.getBlockState(blockPos.below());
		if (blockState2.is(BlockTags.SNOW_LAYER_CANNOT_SURVIVE_ON)) {
			return false;
		} else {
			return blockState2.is(BlockTags.SNOW_LAYER_CAN_SURVIVE_ON)
				? true
				: Block.isFaceFull(blockState2.getCollisionShape(levelReader, blockPos.below()), Direction.UP)
					|| blockState2.is(this) && (Integer)blockState2.getValue(LAYERS) == 8;
		}
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return !blockState.canSurvive(levelAccessor, blockPos)
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	protected boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
		int i = (Integer)blockState.getValue(LAYERS);
		if (!blockPlaceContext.getItemInHand().is(this.asItem()) || i >= 8) {
			return i == 1;
		} else {
			return blockPlaceContext.replacingClickedOnBlock() ? blockPlaceContext.getClickedFace() == Direction.UP : true;
		}
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos());
		if (blockState.is(this)) {
			int i = (Integer)blockState.getValue(LAYERS);
			return blockState.setValue(LAYERS, Integer.valueOf(Math.min(8, i + 1)));
		} else {
			return super.getStateForPlacement(blockPlaceContext);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LAYERS);
	}

	@Override
	protected VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return Shapes.empty();
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return Shapes.empty();
	}

	@Override
	protected VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return Shapes.empty();
	}

	@Override
	protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (!(entity instanceof LivingEntity) || entity.getInBlockState().is(this)) {
			entity.makeStuckInBlock(blockState, new Vec3(0.9F, 1.5, 0.9F));
		}

		int i = (Integer)blockState.getValue(LAYERS);
		if (entity instanceof LivingEntity livingEntity) {
			livingEntity.addEffect(new MobEffectInstance(MobEffects.STICKY, 100 + i * 20));
		}
	}

	@Override
	protected ItemInteractionResult useItemOn(
		ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		return itemStack.is(this.asItem()) ? ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if (!player.canEat(false)) {
			return InteractionResult.PASS;
		} else {
			player.getFoodData().eat(2, 0.1F);
			int i = (Integer)blockState.getValue(LAYERS);
			level.gameEvent(player, GameEvent.EAT, blockPos);
			if (i > 1) {
				level.setBlock(blockPos, blockState.setValue(LAYERS, Integer.valueOf(i - 1)), 3);
			} else {
				level.removeBlock(blockPos, false);
				level.gameEvent(player, GameEvent.BLOCK_DESTROY, blockPos);
			}

			return InteractionResult.sidedSuccess(level.isClientSide());
		}
	}
}
