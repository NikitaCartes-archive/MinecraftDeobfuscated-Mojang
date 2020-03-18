package net.minecraft.world.level.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BeehiveBlock extends BaseEntityBlock {
	public static final Direction[] SPAWN_DIRECTIONS = new Direction[]{Direction.WEST, Direction.EAST, Direction.SOUTH};
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final IntegerProperty HONEY_LEVEL = BlockStateProperties.LEVEL_HONEY;

	public BeehiveBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(HONEY_LEVEL, Integer.valueOf(0)).setValue(FACING, Direction.NORTH));
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return (Integer)blockState.getValue(HONEY_LEVEL);
	}

	@Override
	public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
		super.playerDestroy(level, player, blockPos, blockState, blockEntity, itemStack);
		if (!level.isClientSide && blockEntity instanceof BeehiveBlockEntity) {
			BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
			if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack) == 0) {
				beehiveBlockEntity.emptyAllLivingFromHive(player, blockState, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
				level.updateNeighbourForOutputSignal(blockPos, this);
				this.angerNearbyBees(level, blockPos);
			}

			CriteriaTriggers.BEE_NEST_DESTROYED.trigger((ServerPlayer)player, blockState.getBlock(), itemStack, beehiveBlockEntity.getOccupantCount());
		}
	}

	private void angerNearbyBees(Level level, BlockPos blockPos) {
		List<Bee> list = level.getEntitiesOfClass(Bee.class, new AABB(blockPos).inflate(8.0, 6.0, 8.0));
		if (!list.isEmpty()) {
			List<Player> list2 = level.getEntitiesOfClass(Player.class, new AABB(blockPos).inflate(8.0, 6.0, 8.0));
			int i = list2.size();

			for (Bee bee : list) {
				if (bee.getTarget() == null) {
					bee.makeAngry((Entity)list2.get(level.random.nextInt(i)));
				}
			}
		}
	}

	public static void dropHoneycomb(Level level, BlockPos blockPos) {
		popResource(level, blockPos, new ItemStack(Items.HONEYCOMB, 3));
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		ItemStack itemStack2 = itemStack.copy();
		int i = (Integer)blockState.getValue(HONEY_LEVEL);
		boolean bl = false;
		if (i >= 5) {
			if (itemStack.getItem() == Items.SHEARS) {
				level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BEEHIVE_SHEAR, SoundSource.NEUTRAL, 1.0F, 1.0F);
				dropHoneycomb(level, blockPos);
				itemStack.hurtAndBreak(1, player, playerx -> playerx.broadcastBreakEvent(interactionHand));
				bl = true;
			} else if (itemStack.getItem() == Items.GLASS_BOTTLE) {
				itemStack.shrink(1);
				level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
				if (itemStack.isEmpty()) {
					player.setItemInHand(interactionHand, new ItemStack(Items.HONEY_BOTTLE));
				} else if (!player.inventory.add(new ItemStack(Items.HONEY_BOTTLE))) {
					player.drop(new ItemStack(Items.HONEY_BOTTLE), false);
				}

				bl = true;
			}
		}

		if (bl) {
			if (!CampfireBlock.isSmokeyPos(level, blockPos, 5)) {
				if (this.hiveContainsBees(level, blockPos)) {
					this.angerNearbyBees(level, blockPos);
				}

				this.releaseBeesAndResetHoneyLevel(level, blockState, blockPos, player, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
			} else {
				this.resetHoneyLevel(level, blockState, blockPos);
				if (player instanceof ServerPlayer) {
					CriteriaTriggers.SAFELY_HARVEST_HONEY.trigger((ServerPlayer)player, blockPos, itemStack2);
				}
			}

			return InteractionResult.SUCCESS;
		} else {
			return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
		}
	}

	private boolean hiveContainsBees(Level level, BlockPos blockPos) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof BeehiveBlockEntity) {
			BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
			return !beehiveBlockEntity.isEmpty();
		} else {
			return false;
		}
	}

	public void releaseBeesAndResetHoneyLevel(
		Level level, BlockState blockState, BlockPos blockPos, @Nullable Player player, BeehiveBlockEntity.BeeReleaseStatus beeReleaseStatus
	) {
		this.resetHoneyLevel(level, blockState, blockPos);
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof BeehiveBlockEntity) {
			BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
			beehiveBlockEntity.emptyAllLivingFromHive(player, blockState, beeReleaseStatus);
		}
	}

	public void resetHoneyLevel(Level level, BlockState blockState, BlockPos blockPos) {
		level.setBlock(blockPos, blockState.setValue(HONEY_LEVEL, Integer.valueOf(0)), 3);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		if ((Integer)blockState.getValue(HONEY_LEVEL) >= 5) {
			for (int i = 0; i < random.nextInt(1) + 1; i++) {
				this.trySpawnDripParticles(level, blockPos, blockState);
			}
		}
	}

	@Environment(EnvType.CLIENT)
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

	@Environment(EnvType.CLIENT)
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

	@Environment(EnvType.CLIENT)
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
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockGetter blockGetter) {
		return new BeehiveBlockEntity();
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		if (!level.isClientSide && player.isCreative() && level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
			BlockEntity blockEntity = level.getBlockEntity(blockPos);
			if (blockEntity instanceof BeehiveBlockEntity) {
				BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
				ItemStack itemStack = new ItemStack(this);
				int i = (Integer)blockState.getValue(HONEY_LEVEL);
				boolean bl = !beehiveBlockEntity.isEmpty();
				if (!bl && i == 0) {
					return;
				}

				if (bl) {
					CompoundTag compoundTag = new CompoundTag();
					compoundTag.put("Bees", beehiveBlockEntity.writeBees());
					itemStack.addTagElement("BlockEntityTag", compoundTag);
				}

				CompoundTag compoundTag = new CompoundTag();
				compoundTag.putInt("honey_level", i);
				itemStack.addTagElement("BlockStateTag", compoundTag);
				ItemEntity itemEntity = new ItemEntity(level, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), itemStack);
				itemEntity.setDefaultPickUpDelay();
				level.addFreshEntity(itemEntity);
			}
		}

		super.playerWillDestroy(level, blockPos, blockState, player);
	}

	@Override
	public List<ItemStack> getDrops(BlockState blockState, LootContext.Builder builder) {
		Entity entity = builder.getOptionalParameter(LootContextParams.THIS_ENTITY);
		if (entity instanceof PrimedTnt
			|| entity instanceof Creeper
			|| entity instanceof WitherSkull
			|| entity instanceof WitherBoss
			|| entity instanceof MinecartTNT) {
			BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
			if (blockEntity instanceof BeehiveBlockEntity) {
				BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
				beehiveBlockEntity.emptyAllLivingFromHive(null, blockState, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
			}
		}

		return super.getDrops(blockState, builder);
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (levelAccessor.getBlockState(blockPos2).getBlock() instanceof FireBlock) {
			BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
			if (blockEntity instanceof BeehiveBlockEntity) {
				BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
				beehiveBlockEntity.emptyAllLivingFromHive(null, blockState, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
			}
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}
}
