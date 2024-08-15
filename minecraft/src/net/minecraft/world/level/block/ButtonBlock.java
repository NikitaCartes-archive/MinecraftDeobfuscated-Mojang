package net.minecraft.world.level.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ButtonBlock extends FaceAttachedHorizontalDirectionalBlock {
	public static final MapCodec<ButtonBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					BlockSetType.CODEC.fieldOf("block_set_type").forGetter(buttonBlock -> buttonBlock.type),
					Codec.intRange(1, 1024).fieldOf("ticks_to_stay_pressed").forGetter(buttonBlock -> buttonBlock.ticksToStayPressed),
					propertiesCodec()
				)
				.apply(instance, ButtonBlock::new)
	);
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	private static final int PRESSED_DEPTH = 1;
	private static final int UNPRESSED_DEPTH = 2;
	protected static final int HALF_AABB_HEIGHT = 2;
	protected static final int HALF_AABB_WIDTH = 3;
	protected static final VoxelShape CEILING_AABB_X = Block.box(6.0, 14.0, 5.0, 10.0, 16.0, 11.0);
	protected static final VoxelShape CEILING_AABB_Z = Block.box(5.0, 14.0, 6.0, 11.0, 16.0, 10.0);
	protected static final VoxelShape FLOOR_AABB_X = Block.box(6.0, 0.0, 5.0, 10.0, 2.0, 11.0);
	protected static final VoxelShape FLOOR_AABB_Z = Block.box(5.0, 0.0, 6.0, 11.0, 2.0, 10.0);
	protected static final VoxelShape NORTH_AABB = Block.box(5.0, 6.0, 14.0, 11.0, 10.0, 16.0);
	protected static final VoxelShape SOUTH_AABB = Block.box(5.0, 6.0, 0.0, 11.0, 10.0, 2.0);
	protected static final VoxelShape WEST_AABB = Block.box(14.0, 6.0, 5.0, 16.0, 10.0, 11.0);
	protected static final VoxelShape EAST_AABB = Block.box(0.0, 6.0, 5.0, 2.0, 10.0, 11.0);
	protected static final VoxelShape PRESSED_CEILING_AABB_X = Block.box(6.0, 15.0, 5.0, 10.0, 16.0, 11.0);
	protected static final VoxelShape PRESSED_CEILING_AABB_Z = Block.box(5.0, 15.0, 6.0, 11.0, 16.0, 10.0);
	protected static final VoxelShape PRESSED_FLOOR_AABB_X = Block.box(6.0, 0.0, 5.0, 10.0, 1.0, 11.0);
	protected static final VoxelShape PRESSED_FLOOR_AABB_Z = Block.box(5.0, 0.0, 6.0, 11.0, 1.0, 10.0);
	protected static final VoxelShape PRESSED_NORTH_AABB = Block.box(5.0, 6.0, 15.0, 11.0, 10.0, 16.0);
	protected static final VoxelShape PRESSED_SOUTH_AABB = Block.box(5.0, 6.0, 0.0, 11.0, 10.0, 1.0);
	protected static final VoxelShape PRESSED_WEST_AABB = Block.box(15.0, 6.0, 5.0, 16.0, 10.0, 11.0);
	protected static final VoxelShape PRESSED_EAST_AABB = Block.box(0.0, 6.0, 5.0, 1.0, 10.0, 11.0);
	private final BlockSetType type;
	private final int ticksToStayPressed;

	@Override
	public MapCodec<ButtonBlock> codec() {
		return CODEC;
	}

	protected ButtonBlock(BlockSetType blockSetType, int i, BlockBehaviour.Properties properties) {
		super(properties.sound(blockSetType.soundType()));
		this.type = blockSetType;
		this.registerDefaultState(
			this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(FACE, AttachFace.WALL)
		);
		this.ticksToStayPressed = i;
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		Direction direction = blockState.getValue(FACING);
		boolean bl = (Boolean)blockState.getValue(POWERED);
		switch ((AttachFace)blockState.getValue(FACE)) {
			case FLOOR:
				if (direction.getAxis() == Direction.Axis.X) {
					return bl ? PRESSED_FLOOR_AABB_X : FLOOR_AABB_X;
				}

				return bl ? PRESSED_FLOOR_AABB_Z : FLOOR_AABB_Z;
			case WALL:
				return switch (direction) {
					case EAST -> bl ? PRESSED_EAST_AABB : EAST_AABB;
					case WEST -> bl ? PRESSED_WEST_AABB : WEST_AABB;
					case SOUTH -> bl ? PRESSED_SOUTH_AABB : SOUTH_AABB;
					case NORTH, UP, DOWN -> bl ? PRESSED_NORTH_AABB : NORTH_AABB;
				};
			case CEILING:
			default:
				if (direction.getAxis() == Direction.Axis.X) {
					return bl ? PRESSED_CEILING_AABB_X : CEILING_AABB_X;
				} else {
					return bl ? PRESSED_CEILING_AABB_Z : CEILING_AABB_Z;
				}
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if ((Boolean)blockState.getValue(POWERED)) {
			return InteractionResult.CONSUME;
		} else {
			this.press(blockState, level, blockPos, player);
			return InteractionResult.SUCCESS;
		}
	}

	@Override
	protected void onExplosionHit(
		BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer
	) {
		if (explosion.canTriggerBlocks() && !(Boolean)blockState.getValue(POWERED)) {
			this.press(blockState, serverLevel, blockPos, null);
		}

		super.onExplosionHit(blockState, serverLevel, blockPos, explosion, biConsumer);
	}

	public void press(BlockState blockState, Level level, BlockPos blockPos, @Nullable Player player) {
		level.setBlock(blockPos, blockState.setValue(POWERED, Boolean.valueOf(true)), 3);
		this.updateNeighbours(blockState, level, blockPos);
		level.scheduleTick(blockPos, this, this.ticksToStayPressed);
		this.playSound(player, level, blockPos, true);
		level.gameEvent(player, GameEvent.BLOCK_ACTIVATE, blockPos);
	}

	protected void playSound(@Nullable Player player, LevelAccessor levelAccessor, BlockPos blockPos, boolean bl) {
		levelAccessor.playSound(bl ? player : null, blockPos, this.getSound(bl), SoundSource.BLOCKS);
	}

	protected SoundEvent getSound(boolean bl) {
		return bl ? this.type.buttonClickOn() : this.type.buttonClickOff();
	}

	@Override
	protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!bl && !blockState.is(blockState2.getBlock())) {
			if ((Boolean)blockState.getValue(POWERED)) {
				this.updateNeighbours(blockState, level, blockPos);
			}

			super.onRemove(blockState, level, blockPos, blockState2, bl);
		}
	}

	@Override
	protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return blockState.getValue(POWERED) ? 15 : 0;
	}

	@Override
	protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return blockState.getValue(POWERED) && getConnectedDirection(blockState) == direction ? 15 : 0;
	}

	@Override
	protected boolean isSignalSource(BlockState blockState) {
		return true;
	}

	@Override
	protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if ((Boolean)blockState.getValue(POWERED)) {
			this.checkPressed(blockState, serverLevel, blockPos);
		}
	}

	@Override
	protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (!level.isClientSide && this.type.canButtonBeActivatedByArrows() && !(Boolean)blockState.getValue(POWERED)) {
			this.checkPressed(blockState, level, blockPos);
		}
	}

	protected void checkPressed(BlockState blockState, Level level, BlockPos blockPos) {
		AbstractArrow abstractArrow = this.type.canButtonBeActivatedByArrows()
			? (AbstractArrow)level.getEntitiesOfClass(AbstractArrow.class, blockState.getShape(level, blockPos).bounds().move(blockPos))
				.stream()
				.findFirst()
				.orElse(null)
			: null;
		boolean bl = abstractArrow != null;
		boolean bl2 = (Boolean)blockState.getValue(POWERED);
		if (bl != bl2) {
			level.setBlock(blockPos, blockState.setValue(POWERED, Boolean.valueOf(bl)), 3);
			this.updateNeighbours(blockState, level, blockPos);
			this.playSound(null, level, blockPos, bl);
			level.gameEvent(abstractArrow, bl ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, blockPos);
		}

		if (bl) {
			level.scheduleTick(new BlockPos(blockPos), this, this.ticksToStayPressed);
		}
	}

	private void updateNeighbours(BlockState blockState, Level level, BlockPos blockPos) {
		Direction direction = getConnectedDirection(blockState).getOpposite();
		Orientation orientation = ExperimentalRedstoneUtils.randomOrientation(level, direction, direction.getAxis().isHorizontal() ? Direction.UP : null);
		level.updateNeighborsAt(blockPos, this, orientation);
		level.updateNeighborsAt(blockPos.relative(direction), this, orientation);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED, FACE);
	}
}
