package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CampfireBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
	protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 7.0, 16.0);
	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	public static final BooleanProperty SIGNAL_FIRE = BlockStateProperties.SIGNAL_FIRE;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	private static final VoxelShape VIRTUAL_FENCE_POST = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
	private final boolean spawnParticles;
	private final int fireDamage;

	public CampfireBlock(boolean bl, int i, BlockBehaviour.Properties properties) {
		super(properties);
		this.spawnParticles = bl;
		this.fireDamage = i;
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(LIT, Boolean.valueOf(true))
				.setValue(SIGNAL_FIRE, Boolean.valueOf(false))
				.setValue(WATERLOGGED, Boolean.valueOf(false))
				.setValue(FACING, Direction.NORTH)
		);
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof CampfireBlockEntity) {
			CampfireBlockEntity campfireBlockEntity = (CampfireBlockEntity)blockEntity;
			ItemStack itemStack = player.getItemInHand(interactionHand);
			Optional<CampfireCookingRecipe> optional = campfireBlockEntity.getCookableRecipe(itemStack);
			if (optional.isPresent()) {
				if (!level.isClientSide
					&& campfireBlockEntity.placeFood(player.getAbilities().instabuild ? itemStack.copy() : itemStack, ((CampfireCookingRecipe)optional.get()).getCookingTime())
					)
				 {
					player.awardStat(Stats.INTERACT_WITH_CAMPFIRE);
					return InteractionResult.SUCCESS;
				}

				return InteractionResult.CONSUME;
			}
		}

		return InteractionResult.PASS;
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (!entity.fireImmune() && (Boolean)blockState.getValue(LIT) && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity)) {
			entity.hurt(DamageSource.IN_FIRE, (float)this.fireDamage);
		}

		super.entityInside(blockState, level, blockPos, entity);
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState2.getBlock())) {
			BlockEntity blockEntity = level.getBlockEntity(blockPos);
			if (blockEntity instanceof CampfireBlockEntity) {
				Containers.dropContents(level, blockPos, ((CampfireBlockEntity)blockEntity).getItems());
			}

			super.onRemove(blockState, level, blockPos, blockState2, bl);
		}
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		LevelAccessor levelAccessor = blockPlaceContext.getLevel();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		boolean bl = levelAccessor.getFluidState(blockPos).getType() == Fluids.WATER;
		return this.defaultBlockState()
			.setValue(WATERLOGGED, Boolean.valueOf(bl))
			.setValue(SIGNAL_FIRE, Boolean.valueOf(this.isSmokeSource(levelAccessor.getBlockState(blockPos.below()))))
			.setValue(LIT, Boolean.valueOf(!bl))
			.setValue(FACING, blockPlaceContext.getHorizontalDirection());
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return direction == Direction.DOWN
			? blockState.setValue(SIGNAL_FIRE, Boolean.valueOf(this.isSmokeSource(blockState2)))
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	private boolean isSmokeSource(BlockState blockState) {
		return blockState.is(Blocks.HAY_BLOCK);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		if ((Boolean)blockState.getValue(LIT)) {
			if (random.nextInt(10) == 0) {
				level.playLocalSound(
					(double)blockPos.getX() + 0.5,
					(double)blockPos.getY() + 0.5,
					(double)blockPos.getZ() + 0.5,
					SoundEvents.CAMPFIRE_CRACKLE,
					SoundSource.BLOCKS,
					0.5F + random.nextFloat(),
					random.nextFloat() * 0.7F + 0.6F,
					false
				);
			}

			if (this.spawnParticles && random.nextInt(5) == 0) {
				for (int i = 0; i < random.nextInt(1) + 1; i++) {
					level.addParticle(
						ParticleTypes.LAVA,
						(double)blockPos.getX() + 0.5,
						(double)blockPos.getY() + 0.5,
						(double)blockPos.getZ() + 0.5,
						(double)(random.nextFloat() / 2.0F),
						5.0E-5,
						(double)(random.nextFloat() / 2.0F)
					);
				}
			}
		}
	}

	public static void dowse(@Nullable Entity entity, LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
		if (levelAccessor.isClientSide()) {
			for (int i = 0; i < 20; i++) {
				makeParticles((Level)levelAccessor, blockPos, (Boolean)blockState.getValue(SIGNAL_FIRE), true);
			}
		}

		BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
		if (blockEntity instanceof CampfireBlockEntity) {
			((CampfireBlockEntity)blockEntity).dowse();
		}

		levelAccessor.gameEvent(entity, GameEvent.BLOCK_CHANGE, blockPos);
	}

	@Override
	public boolean placeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
		if (!(Boolean)blockState.getValue(BlockStateProperties.WATERLOGGED) && fluidState.getType() == Fluids.WATER) {
			boolean bl = (Boolean)blockState.getValue(LIT);
			if (bl) {
				if (!levelAccessor.isClientSide()) {
					levelAccessor.playSound(null, blockPos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);
				}

				dowse(null, levelAccessor, blockPos, blockState);
			}

			levelAccessor.setBlock(blockPos, blockState.setValue(WATERLOGGED, Boolean.valueOf(true)).setValue(LIT, Boolean.valueOf(false)), 3);
			levelAccessor.getLiquidTicks().scheduleTick(blockPos, fluidState.getType(), fluidState.getType().getTickDelay(levelAccessor));
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
		if (!level.isClientSide && projectile.isOnFire()) {
			Entity entity = projectile.getOwner();
			boolean bl = entity == null || entity instanceof Player || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
			if (bl && !(Boolean)blockState.getValue(LIT) && !(Boolean)blockState.getValue(WATERLOGGED)) {
				BlockPos blockPos = blockHitResult.getBlockPos();
				level.setBlock(blockPos, blockState.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)), 11);
			}
		}
	}

	public static void makeParticles(Level level, BlockPos blockPos, boolean bl, boolean bl2) {
		Random random = level.getRandom();
		SimpleParticleType simpleParticleType = bl ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE;
		level.addAlwaysVisibleParticle(
			simpleParticleType,
			true,
			(double)blockPos.getX() + 0.5 + random.nextDouble() / 3.0 * (double)(random.nextBoolean() ? 1 : -1),
			(double)blockPos.getY() + random.nextDouble() + random.nextDouble(),
			(double)blockPos.getZ() + 0.5 + random.nextDouble() / 3.0 * (double)(random.nextBoolean() ? 1 : -1),
			0.0,
			0.07,
			0.0
		);
		if (bl2) {
			level.addParticle(
				ParticleTypes.SMOKE,
				(double)blockPos.getX() + 0.25 + random.nextDouble() / 2.0 * (double)(random.nextBoolean() ? 1 : -1),
				(double)blockPos.getY() + 0.4,
				(double)blockPos.getZ() + 0.25 + random.nextDouble() / 2.0 * (double)(random.nextBoolean() ? 1 : -1),
				0.0,
				0.005,
				0.0
			);
		}
	}

	public static boolean isSmokeyPos(Level level, BlockPos blockPos) {
		for (int i = 1; i <= 5; i++) {
			BlockPos blockPos2 = blockPos.below(i);
			BlockState blockState = level.getBlockState(blockPos2);
			if (isLitCampfire(blockState)) {
				return true;
			}

			boolean bl = Shapes.joinIsNotEmpty(VIRTUAL_FENCE_POST, blockState.getCollisionShape(level, blockPos, CollisionContext.empty()), BooleanOp.AND);
			if (bl) {
				BlockState blockState2 = level.getBlockState(blockPos2.below());
				return isLitCampfire(blockState2);
			}
		}

		return false;
	}

	public static boolean isLitCampfire(BlockState blockState) {
		return blockState.hasProperty(LIT) && blockState.is(BlockTags.CAMPFIRES) && (Boolean)blockState.getValue(LIT);
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT, SIGNAL_FIRE, WATERLOGGED, FACING);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new CampfireBlockEntity(blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		if (level.isClientSide) {
			return blockState.getValue(LIT) ? createTickerHelper(blockEntityType, BlockEntityType.CAMPFIRE, CampfireBlockEntity::particleTick) : null;
		} else {
			return blockState.getValue(LIT)
				? createTickerHelper(blockEntityType, BlockEntityType.CAMPFIRE, CampfireBlockEntity::cookTick)
				: createTickerHelper(blockEntityType, BlockEntityType.CAMPFIRE, CampfireBlockEntity::cooldownTick);
		}
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}

	public static boolean canLight(BlockState blockState) {
		return blockState.is(BlockTags.CAMPFIRES, blockStateBase -> blockStateBase.hasProperty(WATERLOGGED) && blockStateBase.hasProperty(LIT))
			&& !(Boolean)blockState.getValue(WATERLOGGED)
			&& !(Boolean)blockState.getValue(LIT);
	}
}
