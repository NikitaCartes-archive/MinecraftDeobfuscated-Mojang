package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class BucketItem extends Item {
	private final Fluid content;

	public BucketItem(Fluid fluid, Item.Properties properties) {
		super(properties);
		this.content = fluid;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		HitResult hitResult = getPlayerPOVHitResult(level, player, this.content == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
		if (hitResult.getType() == HitResult.Type.MISS) {
			return InteractionResultHolder.pass(itemStack);
		} else if (hitResult.getType() != HitResult.Type.BLOCK) {
			return InteractionResultHolder.pass(itemStack);
		} else {
			BlockHitResult blockHitResult = (BlockHitResult)hitResult;
			BlockPos blockPos = blockHitResult.getBlockPos();
			Direction direction = blockHitResult.getDirection();
			BlockPos blockPos2 = blockPos.relative(direction);
			if (!level.mayInteract(player, blockPos) || !player.mayUseItemAt(blockPos2, direction, itemStack)) {
				return InteractionResultHolder.fail(itemStack);
			} else if (this.content == Fluids.EMPTY) {
				BlockState blockState = level.getBlockState(blockPos);
				if (blockState.getBlock() instanceof BucketPickup) {
					Fluid fluid = ((BucketPickup)blockState.getBlock()).takeLiquid(level, blockPos, blockState);
					if (fluid != Fluids.EMPTY) {
						player.awardStat(Stats.ITEM_USED.get(this));
						player.playSound(fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL, 1.0F, 1.0F);
						ItemStack itemStack2 = this.createResultItem(itemStack, player, fluid.getBucket());
						if (!level.isClientSide) {
							CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)player, new ItemStack(fluid.getBucket()));
						}

						return InteractionResultHolder.success(itemStack2);
					}
				}

				return InteractionResultHolder.fail(itemStack);
			} else {
				BlockState blockState = level.getBlockState(blockPos);
				BlockPos blockPos3 = blockState.getBlock() instanceof LiquidBlockContainer && this.content == Fluids.WATER ? blockPos : blockPos2;
				if (this.emptyBucket(player, level, blockPos3, blockHitResult)) {
					this.checkExtraContent(level, itemStack, blockPos3);
					if (player instanceof ServerPlayer) {
						CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockPos3, itemStack);
					}

					player.awardStat(Stats.ITEM_USED.get(this));
					return InteractionResultHolder.success(this.getEmptySuccessItem(itemStack, player));
				} else {
					return InteractionResultHolder.fail(itemStack);
				}
			}
		}
	}

	protected ItemStack getEmptySuccessItem(ItemStack itemStack, Player player) {
		return !player.abilities.instabuild ? new ItemStack(Items.BUCKET) : itemStack;
	}

	public void checkExtraContent(Level level, ItemStack itemStack, BlockPos blockPos) {
	}

	private ItemStack createResultItem(ItemStack itemStack, Player player, Item item) {
		if (player.abilities.instabuild) {
			return itemStack;
		} else {
			itemStack.shrink(1);
			if (itemStack.isEmpty()) {
				return new ItemStack(item);
			} else {
				if (!player.inventory.add(new ItemStack(item))) {
					player.drop(new ItemStack(item), false);
				}

				return itemStack;
			}
		}
	}

	public boolean emptyBucket(@Nullable Player player, Level level, BlockPos blockPos, @Nullable BlockHitResult blockHitResult) {
		if (!(this.content instanceof FlowingFluid)) {
			return false;
		} else {
			BlockState blockState = level.getBlockState(blockPos);
			Material material = blockState.getMaterial();
			boolean bl = blockState.canBeReplaced(this.content);
			if (blockState.isAir()
				|| bl
				|| blockState.getBlock() instanceof LiquidBlockContainer
					&& ((LiquidBlockContainer)blockState.getBlock()).canPlaceLiquid(level, blockPos, blockState, this.content)) {
				if (level.dimensionType().ultraWarm() && this.content.is(FluidTags.WATER)) {
					int i = blockPos.getX();
					int j = blockPos.getY();
					int k = blockPos.getZ();
					level.playSound(
						player, blockPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F
					);

					for (int l = 0; l < 8; l++) {
						level.addParticle(ParticleTypes.LARGE_SMOKE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0, 0.0, 0.0);
					}
				} else {
					if (!(blockState.getBlock() instanceof LiquidBlockContainer) || this.content != Fluids.WATER) {
						if (!level.isClientSide && bl && !material.isLiquid()) {
							level.destroyBlock(blockPos, true);
						}

						this.playEmptySound(player, level, blockPos);
						return level.setBlock(blockPos, this.content.defaultFluidState().createLegacyBlock(), 11);
					}

					if (((LiquidBlockContainer)blockState.getBlock()).placeLiquid(level, blockPos, blockState, ((FlowingFluid)this.content).getSource(false))) {
						this.playEmptySound(player, level, blockPos);
					}
				}

				return true;
			} else {
				return blockHitResult == null ? false : this.emptyBucket(player, level, blockHitResult.getBlockPos().relative(blockHitResult.getDirection()), null);
			}
		}
	}

	protected void playEmptySound(@Nullable Player player, LevelAccessor levelAccessor, BlockPos blockPos) {
		SoundEvent soundEvent = this.content.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
		levelAccessor.playSound(player, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
	}
}
