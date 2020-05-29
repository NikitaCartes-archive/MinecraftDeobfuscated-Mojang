package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CauldronBlock extends Block {
	public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;
	private static final VoxelShape INSIDE = box(2.0, 4.0, 2.0, 14.0, 16.0, 14.0);
	protected static final VoxelShape SHAPE = Shapes.join(
		Shapes.block(),
		Shapes.or(box(0.0, 0.0, 4.0, 16.0, 3.0, 12.0), box(4.0, 0.0, 0.0, 12.0, 3.0, 16.0), box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0), INSIDE),
		BooleanOp.ONLY_FIRST
	);

	public CauldronBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(0)));
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return INSIDE;
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		int i = (Integer)blockState.getValue(LEVEL);
		float f = (float)blockPos.getY() + (6.0F + (float)(3 * i)) / 16.0F;
		if (!level.isClientSide && entity.isOnFire() && i > 0 && entity.getY() <= (double)f) {
			entity.clearFire();
			this.setWaterLevel(level, blockPos, blockState, i - 1);
		}
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (itemStack.isEmpty()) {
			return InteractionResult.PASS;
		} else {
			int i = (Integer)blockState.getValue(LEVEL);
			Item item = itemStack.getItem();
			if (item == Items.WATER_BUCKET) {
				if (i < 3 && !level.isClientSide) {
					if (!player.abilities.instabuild) {
						player.setItemInHand(interactionHand, new ItemStack(Items.BUCKET));
					}

					player.awardStat(Stats.FILL_CAULDRON);
					this.setWaterLevel(level, blockPos, blockState, 3);
					level.playSound(null, blockPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
				}

				return InteractionResult.sidedSuccess(level.isClientSide);
			} else if (item == Items.BUCKET) {
				if (i == 3 && !level.isClientSide) {
					if (!player.abilities.instabuild) {
						itemStack.shrink(1);
						if (itemStack.isEmpty()) {
							player.setItemInHand(interactionHand, new ItemStack(Items.WATER_BUCKET));
						} else if (!player.inventory.add(new ItemStack(Items.WATER_BUCKET))) {
							player.drop(new ItemStack(Items.WATER_BUCKET), false);
						}
					}

					player.awardStat(Stats.USE_CAULDRON);
					this.setWaterLevel(level, blockPos, blockState, 0);
					level.playSound(null, blockPos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
				}

				return InteractionResult.sidedSuccess(level.isClientSide);
			} else if (item == Items.GLASS_BOTTLE) {
				if (i > 0 && !level.isClientSide) {
					if (!player.abilities.instabuild) {
						ItemStack itemStack2 = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
						player.awardStat(Stats.USE_CAULDRON);
						itemStack.shrink(1);
						if (itemStack.isEmpty()) {
							player.setItemInHand(interactionHand, itemStack2);
						} else if (!player.inventory.add(itemStack2)) {
							player.drop(itemStack2, false);
						} else if (player instanceof ServerPlayer) {
							((ServerPlayer)player).refreshContainer(player.inventoryMenu);
						}
					}

					level.playSound(null, blockPos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
					this.setWaterLevel(level, blockPos, blockState, i - 1);
				}

				return InteractionResult.sidedSuccess(level.isClientSide);
			} else if (item == Items.POTION && PotionUtils.getPotion(itemStack) == Potions.WATER) {
				if (i < 3 && !level.isClientSide) {
					if (!player.abilities.instabuild) {
						ItemStack itemStack2 = new ItemStack(Items.GLASS_BOTTLE);
						player.awardStat(Stats.USE_CAULDRON);
						player.setItemInHand(interactionHand, itemStack2);
						if (player instanceof ServerPlayer) {
							((ServerPlayer)player).refreshContainer(player.inventoryMenu);
						}
					}

					level.playSound(null, blockPos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
					this.setWaterLevel(level, blockPos, blockState, i + 1);
				}

				return InteractionResult.sidedSuccess(level.isClientSide);
			} else {
				if (i > 0 && item instanceof DyeableLeatherItem) {
					DyeableLeatherItem dyeableLeatherItem = (DyeableLeatherItem)item;
					if (dyeableLeatherItem.hasCustomColor(itemStack) && !level.isClientSide) {
						dyeableLeatherItem.clearColor(itemStack);
						this.setWaterLevel(level, blockPos, blockState, i - 1);
						player.awardStat(Stats.CLEAN_ARMOR);
						return InteractionResult.SUCCESS;
					}
				}

				if (i > 0 && item instanceof BannerItem) {
					if (BannerBlockEntity.getPatternCount(itemStack) > 0 && !level.isClientSide) {
						ItemStack itemStack2 = itemStack.copy();
						itemStack2.setCount(1);
						BannerBlockEntity.removeLastPattern(itemStack2);
						player.awardStat(Stats.CLEAN_BANNER);
						if (!player.abilities.instabuild) {
							itemStack.shrink(1);
							this.setWaterLevel(level, blockPos, blockState, i - 1);
						}

						if (itemStack.isEmpty()) {
							player.setItemInHand(interactionHand, itemStack2);
						} else if (!player.inventory.add(itemStack2)) {
							player.drop(itemStack2, false);
						} else if (player instanceof ServerPlayer) {
							((ServerPlayer)player).refreshContainer(player.inventoryMenu);
						}
					}

					return InteractionResult.sidedSuccess(level.isClientSide);
				} else if (i > 0 && item instanceof BlockItem) {
					Block block = ((BlockItem)item).getBlock();
					if (block instanceof ShulkerBoxBlock && !level.isClientSide()) {
						ItemStack itemStack3 = new ItemStack(Blocks.SHULKER_BOX, 1);
						if (itemStack.hasTag()) {
							itemStack3.setTag(itemStack.getTag().copy());
						}

						player.setItemInHand(interactionHand, itemStack3);
						this.setWaterLevel(level, blockPos, blockState, i - 1);
						player.awardStat(Stats.CLEAN_SHULKER_BOX);
						return InteractionResult.SUCCESS;
					} else {
						return InteractionResult.CONSUME;
					}
				} else {
					return InteractionResult.PASS;
				}
			}
		}
	}

	public void setWaterLevel(Level level, BlockPos blockPos, BlockState blockState, int i) {
		level.setBlock(blockPos, blockState.setValue(LEVEL, Integer.valueOf(Mth.clamp(i, 0, 3))), 2);
		level.updateNeighbourForOutputSignal(blockPos, this);
	}

	@Override
	public void handleRain(Level level, BlockPos blockPos) {
		if (level.random.nextInt(20) == 1) {
			float f = level.getBiome(blockPos).getTemperature(blockPos);
			if (!(f < 0.15F)) {
				BlockState blockState = level.getBlockState(blockPos);
				if ((Integer)blockState.getValue(LEVEL) < 3) {
					level.setBlock(blockPos, blockState.cycle(LEVEL), 2);
				}
			}
		}
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return (Integer)blockState.getValue(LEVEL);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LEVEL);
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}
}
