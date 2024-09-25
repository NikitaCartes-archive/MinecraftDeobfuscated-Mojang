package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BeehiveBlock extends BaseEntityBlock {
	public static final MapCodec<BeehiveBlock> CODEC = simpleCodec(BeehiveBlock::new);
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final IntegerProperty HONEY_LEVEL = BlockStateProperties.LEVEL_HONEY;
	public static final int MAX_HONEY_LEVELS = 5;
	private static final int SHEARED_HONEYCOMB_COUNT = 3;

	@Override
	public MapCodec<BeehiveBlock> codec() {
		return CODEC;
	}

	public BeehiveBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(HONEY_LEVEL, Integer.valueOf(0)).setValue(FACING, Direction.NORTH));
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return (Integer)blockState.getValue(HONEY_LEVEL);
	}

	@Override
	public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
		super.playerDestroy(level, player, blockPos, blockState, blockEntity, itemStack);
		if (!level.isClientSide && blockEntity instanceof BeehiveBlockEntity beehiveBlockEntity) {
			if (!EnchantmentHelper.hasTag(itemStack, EnchantmentTags.PREVENTS_BEE_SPAWNS_WHEN_MINING)) {
				beehiveBlockEntity.emptyAllLivingFromHive(player, blockState, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
				level.updateNeighbourForOutputSignal(blockPos, this);
				this.angerNearbyBees(level, blockPos);
			}

			CriteriaTriggers.BEE_NEST_DESTROYED.trigger((ServerPlayer)player, blockState, itemStack, beehiveBlockEntity.getOccupantCount());
		}
	}

	@Override
	protected void onExplosionHit(
		BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Explosion explosion, BiConsumer<ItemStack, BlockPos> biConsumer
	) {
		super.onExplosionHit(blockState, serverLevel, blockPos, explosion, biConsumer);
		this.angerNearbyBees(serverLevel, blockPos);
	}

	private void angerNearbyBees(Level level, BlockPos blockPos) {
		AABB aABB = new AABB(blockPos).inflate(8.0, 6.0, 8.0);
		List<Bee> list = level.getEntitiesOfClass(Bee.class, aABB);
		if (!list.isEmpty()) {
			List<Player> list2 = level.getEntitiesOfClass(Player.class, aABB);
			if (list2.isEmpty()) {
				return;
			}

			for (Bee bee : list) {
				if (bee.getTarget() == null) {
					Player player = Util.getRandom(list2, level.random);
					bee.setTarget(player);
				}
			}
		}
	}

	public static void dropHoneycomb(Level level, BlockPos blockPos) {
		popResource(level, blockPos, new ItemStack(Items.HONEYCOMB, 3));
	}

	@Override
	protected InteractionResult useItemOn(
		ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		int i = (Integer)blockState.getValue(HONEY_LEVEL);
		boolean bl = false;
		if (i >= 5) {
			Item item = itemStack.getItem();
			if (itemStack.is(Items.SHEARS)) {
				level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
				dropHoneycomb(level, blockPos);
				itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(interactionHand));
				bl = true;
				level.gameEvent(player, GameEvent.SHEAR, blockPos);
			} else if (itemStack.is(Items.GLASS_BOTTLE)) {
				itemStack.shrink(1);
				level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
				if (itemStack.isEmpty()) {
					player.setItemInHand(interactionHand, new ItemStack(Items.HONEY_BOTTLE));
				} else if (!player.getInventory().add(new ItemStack(Items.HONEY_BOTTLE))) {
					player.drop(new ItemStack(Items.HONEY_BOTTLE), false);
				}

				bl = true;
				level.gameEvent(player, GameEvent.FLUID_PICKUP, blockPos);
			}

			if (!level.isClientSide() && bl) {
				player.awardStat(Stats.ITEM_USED.get(item));
			}
		}

		if (bl) {
			if (!CampfireBlock.isSmokeyPos(level, blockPos)) {
				if (this.hiveContainsBees(level, blockPos)) {
					this.angerNearbyBees(level, blockPos);
				}

				this.releaseBeesAndResetHoneyLevel(level, blockState, blockPos, player, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
			} else {
				this.resetHoneyLevel(level, blockState, blockPos);
			}

			return InteractionResult.SUCCESS;
		} else {
			return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
		}
	}

	private boolean hiveContainsBees(Level level, BlockPos blockPos) {
		return level.getBlockEntity(blockPos) instanceof BeehiveBlockEntity beehiveBlockEntity ? !beehiveBlockEntity.isEmpty() : false;
	}

	public void releaseBeesAndResetHoneyLevel(
		Level level, BlockState blockState, BlockPos blockPos, @Nullable Player player, BeehiveBlockEntity.BeeReleaseStatus beeReleaseStatus
	) {
		this.resetHoneyLevel(level, blockState, blockPos);
		if (level.getBlockEntity(blockPos) instanceof BeehiveBlockEntity beehiveBlockEntity) {
			beehiveBlockEntity.emptyAllLivingFromHive(player, blockState, beeReleaseStatus);
		}
	}

	public void resetHoneyLevel(Level level, BlockState blockState, BlockPos blockPos) {
		level.setBlock(blockPos, blockState.setValue(HONEY_LEVEL, Integer.valueOf(0)), 3);
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		if ((Integer)blockState.getValue(HONEY_LEVEL) >= 5) {
			for (int i = 0; i < randomSource.nextInt(1) + 1; i++) {
				this.trySpawnDripParticles(level, blockPos, blockState);
			}
		}
	}

	private void trySpawnDripParticles(Level level, BlockPos blockPos, BlockState blockState) {
		if (blockState.getFluidState().isEmpty() && !(level.random.nextFloat() < 0.3F)) {
			VoxelShape voxelShape = blockState.getCollisionShape(level, blockPos);
			double d = voxelShape.max(Direction.Axis.Y);
			if (d >= 1.0 && !blockState.is(BlockTags.IMPERMEABLE)) {
				double e = voxelShape.min(Direction.Axis.Y);
				if (e > 0.0) {
					this.spawnParticle(level, blockPos, voxelShape, (double)blockPos.getY() + e - 0.05);
				} else {
					BlockPos blockPos2 = blockPos.below();
					BlockState blockState2 = level.getBlockState(blockPos2);
					VoxelShape voxelShape2 = blockState2.getCollisionShape(level, blockPos2);
					double f = voxelShape2.max(Direction.Axis.Y);
					if ((f < 1.0 || !blockState2.isCollisionShapeFullBlock(level, blockPos2)) && blockState2.getFluidState().isEmpty()) {
						this.spawnParticle(level, blockPos, voxelShape, (double)blockPos.getY() - 0.05);
					}
				}
			}
		}
	}

	private void spawnParticle(Level level, BlockPos blockPos, VoxelShape voxelShape, double d) {
		this.spawnFluidParticle(
			level,
			(double)blockPos.getX() + voxelShape.min(Direction.Axis.X),
			(double)blockPos.getX() + voxelShape.max(Direction.Axis.X),
			(double)blockPos.getZ() + voxelShape.min(Direction.Axis.Z),
			(double)blockPos.getZ() + voxelShape.max(Direction.Axis.Z),
			d
		);
	}

	private void spawnFluidParticle(Level level, double d, double e, double f, double g, double h) {
		level.addParticle(ParticleTypes.DRIPPING_HONEY, Mth.lerp(level.random.nextDouble(), d, e), h, Mth.lerp(level.random.nextDouble(), f, g), 0.0, 0.0, 0.0);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HONEY_LEVEL, FACING);
	}

	@Override
	protected RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new BeehiveBlockEntity(blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return level.isClientSide ? null : createTickerHelper(blockEntityType, BlockEntityType.BEEHIVE, BeehiveBlockEntity::serverTick);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		if (level instanceof ServerLevel serverLevel
			&& player.isCreative()
			&& serverLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)
			&& level.getBlockEntity(blockPos) instanceof BeehiveBlockEntity beehiveBlockEntity) {
			int i = (Integer)blockState.getValue(HONEY_LEVEL);
			boolean bl = !beehiveBlockEntity.isEmpty();
			if (bl || i > 0) {
				ItemStack itemStack = new ItemStack(this);
				itemStack.applyComponents(beehiveBlockEntity.collectComponents());
				itemStack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(HONEY_LEVEL, i));
				ItemEntity itemEntity = new ItemEntity(level, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), itemStack);
				itemEntity.setDefaultPickUpDelay();
				level.addFreshEntity(itemEntity);
			}
		}

		return super.playerWillDestroy(level, blockPos, blockState, player);
	}

	@Override
	protected List<ItemStack> getDrops(BlockState blockState, LootParams.Builder builder) {
		Entity entity = builder.getOptionalParameter(LootContextParams.THIS_ENTITY);
		if (entity instanceof PrimedTnt
			|| entity instanceof Creeper
			|| entity instanceof WitherSkull
			|| entity instanceof WitherBoss
			|| entity instanceof MinecartTNT) {
			BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
			if (blockEntity instanceof BeehiveBlockEntity beehiveBlockEntity) {
				beehiveBlockEntity.emptyAllLivingFromHive(null, blockState, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
			}
		}

		return super.getDrops(blockState, builder);
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState,
		LevelReader levelReader,
		ScheduledTickAccess scheduledTickAccess,
		BlockPos blockPos,
		Direction direction,
		BlockPos blockPos2,
		BlockState blockState2,
		RandomSource randomSource
	) {
		if (levelReader.getBlockState(blockPos2).getBlock() instanceof FireBlock
			&& levelReader.getBlockEntity(blockPos) instanceof BeehiveBlockEntity beehiveBlockEntity) {
			beehiveBlockEntity.emptyAllLivingFromHive(null, blockState, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
		}

		return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
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
	public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
		BlockItemStateProperties blockItemStateProperties = itemStack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
		int i = (Integer)Objects.requireNonNullElse((Integer)blockItemStateProperties.get(HONEY_LEVEL), 0);
		int j = itemStack.getOrDefault(DataComponents.BEES, List.of()).size();
		list.add(Component.translatable("container.beehive.bees", j, 3).withStyle(ChatFormatting.GRAY));
		list.add(Component.translatable("container.beehive.honey", i, 5).withStyle(ChatFormatting.GRAY));
	}
}
