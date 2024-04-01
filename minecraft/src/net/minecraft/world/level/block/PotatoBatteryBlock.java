package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PotatoBatteryBlock extends Block {
	public static final MapCodec<PotatoBatteryBlock> CODEC = simpleCodec(PotatoBatteryBlock::new);
	public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;
	protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0);

	@Override
	public MapCodec<PotatoBatteryBlock> codec() {
		return CODEC;
	}

	public PotatoBatteryBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(INVERTED, Boolean.valueOf(false)));
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	protected boolean useShapeForLightOcclusion(BlockState blockState) {
		return true;
	}

	@Override
	protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return blockState.getValue(INVERTED) ? 15 : 0;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if (player.mayBuild()) {
			if (level.isClientSide) {
				return InteractionResult.SUCCESS;
			} else {
				BlockState blockState2 = blockState.cycle(INVERTED);
				level.setBlock(blockPos, blockState2, 3);
				level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, blockState2));
				return InteractionResult.CONSUME;
			}
		} else {
			return super.useWithoutItem(blockState, level, blockPos, player, blockHitResult);
		}
	}

	@Override
	protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (entity instanceof LivingEntity livingEntity && (Boolean)blockState.getValue(INVERTED) && level instanceof ServerLevel serverLevel) {
			livingEntity.hurt(level.damageSources().potatoMagic(), 0.5F);

			for (float f = 0.2F; f < 0.8F; f += 0.1F) {
				serverLevel.sendParticles(
					ParticleTypes.ELECTRIC_SPARK,
					(double)((float)blockPos.getX() + f),
					(double)blockPos.getY() + 0.35,
					(double)((float)blockPos.getZ() + f),
					1,
					0.05,
					0.05,
					0.05,
					0.1
				);
			}

			serverLevel.playSound(null, blockPos, SoundEvents.BATTERY_ZAP, SoundSource.BLOCKS, 0.5F, 2.0F);
		}

		super.entityInside(blockState, level, blockPos, entity);
	}

	@Override
	protected RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	protected boolean isSignalSource(BlockState blockState) {
		return true;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(INVERTED);
	}
}
