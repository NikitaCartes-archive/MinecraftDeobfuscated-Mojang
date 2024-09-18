package net.minecraft.core.dispenser;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public interface DispenseItemBehavior {
	Logger LOGGER = LogUtils.getLogger();
	DispenseItemBehavior NOOP = (blockSource, itemStack) -> itemStack;

	ItemStack dispense(BlockSource blockSource, ItemStack itemStack);

	static void bootStrap() {
		DispenserBlock.registerProjectileBehavior(Items.ARROW);
		DispenserBlock.registerProjectileBehavior(Items.TIPPED_ARROW);
		DispenserBlock.registerProjectileBehavior(Items.SPECTRAL_ARROW);
		DispenserBlock.registerProjectileBehavior(Items.EGG);
		DispenserBlock.registerProjectileBehavior(Items.SNOWBALL);
		DispenserBlock.registerProjectileBehavior(Items.EXPERIENCE_BOTTLE);
		DispenserBlock.registerProjectileBehavior(Items.SPLASH_POTION);
		DispenserBlock.registerProjectileBehavior(Items.LINGERING_POTION);
		DispenserBlock.registerProjectileBehavior(Items.FIREWORK_ROCKET);
		DispenserBlock.registerProjectileBehavior(Items.FIRE_CHARGE);
		DispenserBlock.registerProjectileBehavior(Items.WIND_CHARGE);
		DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior() {
			@Override
			public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
				EntityType<?> entityType = ((SpawnEggItem)itemStack.getItem()).getType(itemStack);

				try {
					entityType.spawn(
						blockSource.level(), itemStack, null, blockSource.pos().relative(direction), EntitySpawnReason.DISPENSER, direction != Direction.UP, false
					);
				} catch (Exception var6) {
					LOGGER.error("Error while dispensing spawn egg from dispenser at {}", blockSource.pos(), var6);
					return ItemStack.EMPTY;
				}

				itemStack.shrink(1);
				blockSource.level().gameEvent(null, GameEvent.ENTITY_PLACE, blockSource.pos());
				return itemStack;
			}
		};

		for (SpawnEggItem spawnEggItem : SpawnEggItem.eggs()) {
			DispenserBlock.registerBehavior(spawnEggItem, defaultDispenseItemBehavior);
		}

		DispenserBlock.registerBehavior(Items.ARMOR_STAND, new DefaultDispenseItemBehavior() {
			@Override
			public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
				BlockPos blockPos = blockSource.pos().relative(direction);
				ServerLevel serverLevel = blockSource.level();
				Consumer<ArmorStand> consumer = EntityType.appendDefaultStackConfig(armorStandx -> armorStandx.setYRot(direction.toYRot()), serverLevel, itemStack, null);
				ArmorStand armorStand = EntityType.ARMOR_STAND.spawn(serverLevel, consumer, blockPos, EntitySpawnReason.DISPENSER, false, false);
				if (armorStand != null) {
					itemStack.shrink(1);
				}

				return itemStack;
			}
		});
		DispenserBlock.registerBehavior(
			Items.SADDLE,
			new OptionalDispenseItemBehavior() {
				@Override
				public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
					BlockPos blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
					List<LivingEntity> list = blockSource.level()
						.getEntitiesOfClass(
							LivingEntity.class,
							new AABB(blockPos),
							livingEntity -> !(livingEntity instanceof Saddleable saddleable) ? false : !saddleable.isSaddled() && saddleable.isSaddleable()
						);
					if (!list.isEmpty()) {
						((Saddleable)list.get(0)).equipSaddle(itemStack.split(1), SoundSource.BLOCKS);
						this.setSuccess(true);
						return itemStack;
					} else {
						return super.execute(blockSource, itemStack);
					}
				}
			}
		);
		DispenserBlock.registerBehavior(
			Items.CHEST,
			new OptionalDispenseItemBehavior() {
				@Override
				public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
					BlockPos blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));

					for (AbstractChestedHorse abstractChestedHorse : blockSource.level()
						.getEntitiesOfClass(
							AbstractChestedHorse.class, new AABB(blockPos), abstractChestedHorsex -> abstractChestedHorsex.isAlive() && !abstractChestedHorsex.hasChest()
						)) {
						if (abstractChestedHorse.isTamed() && abstractChestedHorse.getSlot(499).set(itemStack)) {
							itemStack.shrink(1);
							this.setSuccess(true);
							return itemStack;
						}
					}

					return super.execute(blockSource, itemStack);
				}
			}
		);
		DispenserBlock.registerBehavior(Items.OAK_BOAT, new BoatDispenseItemBehavior(Boat.Type.OAK));
		DispenserBlock.registerBehavior(Items.SPRUCE_BOAT, new BoatDispenseItemBehavior(Boat.Type.SPRUCE));
		DispenserBlock.registerBehavior(Items.BIRCH_BOAT, new BoatDispenseItemBehavior(Boat.Type.BIRCH));
		DispenserBlock.registerBehavior(Items.JUNGLE_BOAT, new BoatDispenseItemBehavior(Boat.Type.JUNGLE));
		DispenserBlock.registerBehavior(Items.DARK_OAK_BOAT, new BoatDispenseItemBehavior(Boat.Type.DARK_OAK));
		DispenserBlock.registerBehavior(Items.ACACIA_BOAT, new BoatDispenseItemBehavior(Boat.Type.ACACIA));
		DispenserBlock.registerBehavior(Items.CHERRY_BOAT, new BoatDispenseItemBehavior(Boat.Type.CHERRY));
		DispenserBlock.registerBehavior(Items.MANGROVE_BOAT, new BoatDispenseItemBehavior(Boat.Type.MANGROVE));
		DispenserBlock.registerBehavior(Items.BAMBOO_RAFT, new BoatDispenseItemBehavior(Boat.Type.BAMBOO));
		DispenserBlock.registerBehavior(Items.OAK_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.OAK, true));
		DispenserBlock.registerBehavior(Items.SPRUCE_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.SPRUCE, true));
		DispenserBlock.registerBehavior(Items.BIRCH_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.BIRCH, true));
		DispenserBlock.registerBehavior(Items.JUNGLE_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.JUNGLE, true));
		DispenserBlock.registerBehavior(Items.DARK_OAK_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.DARK_OAK, true));
		DispenserBlock.registerBehavior(Items.ACACIA_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.ACACIA, true));
		DispenserBlock.registerBehavior(Items.CHERRY_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.CHERRY, true));
		DispenserBlock.registerBehavior(Items.MANGROVE_CHEST_BOAT, new BoatDispenseItemBehavior(Boat.Type.MANGROVE, true));
		DispenserBlock.registerBehavior(Items.BAMBOO_CHEST_RAFT, new BoatDispenseItemBehavior(Boat.Type.BAMBOO, true));
		DispenseItemBehavior dispenseItemBehavior = new DefaultDispenseItemBehavior() {
			private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

			@Override
			public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				DispensibleContainerItem dispensibleContainerItem = (DispensibleContainerItem)itemStack.getItem();
				BlockPos blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
				Level level = blockSource.level();
				if (dispensibleContainerItem.emptyContents(null, level, blockPos, null)) {
					dispensibleContainerItem.checkExtraContent(null, level, itemStack, blockPos);
					return this.consumeWithRemainder(blockSource, itemStack, new ItemStack(Items.BUCKET));
				} else {
					return this.defaultDispenseItemBehavior.dispense(blockSource, itemStack);
				}
			}
		};
		DispenserBlock.registerBehavior(Items.LAVA_BUCKET, dispenseItemBehavior);
		DispenserBlock.registerBehavior(Items.WATER_BUCKET, dispenseItemBehavior);
		DispenserBlock.registerBehavior(Items.POWDER_SNOW_BUCKET, dispenseItemBehavior);
		DispenserBlock.registerBehavior(Items.SALMON_BUCKET, dispenseItemBehavior);
		DispenserBlock.registerBehavior(Items.COD_BUCKET, dispenseItemBehavior);
		DispenserBlock.registerBehavior(Items.PUFFERFISH_BUCKET, dispenseItemBehavior);
		DispenserBlock.registerBehavior(Items.TROPICAL_FISH_BUCKET, dispenseItemBehavior);
		DispenserBlock.registerBehavior(Items.AXOLOTL_BUCKET, dispenseItemBehavior);
		DispenserBlock.registerBehavior(Items.TADPOLE_BUCKET, dispenseItemBehavior);
		DispenserBlock.registerBehavior(Items.BUCKET, new DefaultDispenseItemBehavior() {
			@Override
			public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				LevelAccessor levelAccessor = blockSource.level();
				BlockPos blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
				BlockState blockState = levelAccessor.getBlockState(blockPos);
				if (blockState.getBlock() instanceof BucketPickup bucketPickup) {
					ItemStack itemStack2 = bucketPickup.pickupBlock(null, levelAccessor, blockPos, blockState);
					if (itemStack2.isEmpty()) {
						return super.execute(blockSource, itemStack);
					} else {
						levelAccessor.gameEvent(null, GameEvent.FLUID_PICKUP, blockPos);
						Item item = itemStack2.getItem();
						return this.consumeWithRemainder(blockSource, itemStack, new ItemStack(item));
					}
				} else {
					return super.execute(blockSource, itemStack);
				}
			}
		});
		DispenserBlock.registerBehavior(Items.FLINT_AND_STEEL, new OptionalDispenseItemBehavior() {
			@Override
			protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				ServerLevel serverLevel = blockSource.level();
				this.setSuccess(true);
				Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
				BlockPos blockPos = blockSource.pos().relative(direction);
				BlockState blockState = serverLevel.getBlockState(blockPos);
				if (BaseFireBlock.canBePlacedAt(serverLevel, blockPos, direction)) {
					serverLevel.setBlockAndUpdate(blockPos, BaseFireBlock.getState(serverLevel, blockPos));
					serverLevel.gameEvent(null, GameEvent.BLOCK_PLACE, blockPos);
				} else if (CampfireBlock.canLight(blockState) || CandleBlock.canLight(blockState) || CandleCakeBlock.canLight(blockState)) {
					serverLevel.setBlockAndUpdate(blockPos, blockState.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)));
					serverLevel.gameEvent(null, GameEvent.BLOCK_CHANGE, blockPos);
				} else if (blockState.getBlock() instanceof TntBlock) {
					TntBlock.explode(serverLevel, blockPos);
					serverLevel.removeBlock(blockPos, false);
				} else {
					this.setSuccess(false);
				}

				if (this.isSuccess()) {
					itemStack.hurtAndBreak(1, serverLevel, null, item -> {
					});
				}

				return itemStack;
			}
		});
		DispenserBlock.registerBehavior(Items.BONE_MEAL, new OptionalDispenseItemBehavior() {
			@Override
			protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				this.setSuccess(true);
				Level level = blockSource.level();
				BlockPos blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
				if (!BoneMealItem.growCrop(itemStack, level, blockPos) && !BoneMealItem.growWaterPlant(itemStack, level, blockPos, null)) {
					this.setSuccess(false);
				} else if (!level.isClientSide) {
					level.levelEvent(1505, blockPos, 15);
				}

				return itemStack;
			}
		});
		DispenserBlock.registerBehavior(Blocks.TNT, new DefaultDispenseItemBehavior() {
			@Override
			protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				Level level = blockSource.level();
				BlockPos blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
				PrimedTnt primedTnt = new PrimedTnt(level, (double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5, null);
				level.addFreshEntity(primedTnt);
				level.playSound(null, primedTnt.getX(), primedTnt.getY(), primedTnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
				level.gameEvent(null, GameEvent.ENTITY_PLACE, blockPos);
				itemStack.shrink(1);
				return itemStack;
			}
		});
		DispenserBlock.registerBehavior(
			Items.WITHER_SKELETON_SKULL,
			new OptionalDispenseItemBehavior() {
				@Override
				protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
					Level level = blockSource.level();
					Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
					BlockPos blockPos = blockSource.pos().relative(direction);
					if (level.isEmptyBlock(blockPos) && WitherSkullBlock.canSpawnMob(level, blockPos, itemStack)) {
						level.setBlock(
							blockPos,
							Blocks.WITHER_SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, Integer.valueOf(RotationSegment.convertToSegment(direction))),
							3
						);
						level.gameEvent(null, GameEvent.BLOCK_PLACE, blockPos);
						BlockEntity blockEntity = level.getBlockEntity(blockPos);
						if (blockEntity instanceof SkullBlockEntity) {
							WitherSkullBlock.checkSpawn(level, blockPos, (SkullBlockEntity)blockEntity);
						}

						itemStack.shrink(1);
						this.setSuccess(true);
					} else {
						this.setSuccess(EquipmentDispenseItemBehavior.dispenseEquipment(blockSource, itemStack));
					}

					return itemStack;
				}
			}
		);
		DispenserBlock.registerBehavior(Blocks.CARVED_PUMPKIN, new OptionalDispenseItemBehavior() {
			@Override
			protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				Level level = blockSource.level();
				BlockPos blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
				CarvedPumpkinBlock carvedPumpkinBlock = (CarvedPumpkinBlock)Blocks.CARVED_PUMPKIN;
				if (level.isEmptyBlock(blockPos) && carvedPumpkinBlock.canSpawnGolem(level, blockPos)) {
					if (!level.isClientSide) {
						level.setBlock(blockPos, carvedPumpkinBlock.defaultBlockState(), 3);
						level.gameEvent(null, GameEvent.BLOCK_PLACE, blockPos);
					}

					itemStack.shrink(1);
					this.setSuccess(true);
				} else {
					this.setSuccess(EquipmentDispenseItemBehavior.dispenseEquipment(blockSource, itemStack));
				}

				return itemStack;
			}
		});
		DispenserBlock.registerBehavior(Blocks.SHULKER_BOX.asItem(), new ShulkerBoxDispenseBehavior());

		for (DyeColor dyeColor : DyeColor.values()) {
			DispenserBlock.registerBehavior(ShulkerBoxBlock.getBlockByColor(dyeColor).asItem(), new ShulkerBoxDispenseBehavior());
		}

		DispenserBlock.registerBehavior(
			Items.GLASS_BOTTLE.asItem(),
			new OptionalDispenseItemBehavior() {
				private ItemStack takeLiquid(BlockSource blockSource, ItemStack itemStack, ItemStack itemStack2) {
					blockSource.level().gameEvent(null, GameEvent.FLUID_PICKUP, blockSource.pos());
					return this.consumeWithRemainder(blockSource, itemStack, itemStack2);
				}

				@Override
				public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
					this.setSuccess(false);
					ServerLevel serverLevel = blockSource.level();
					BlockPos blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
					BlockState blockState = serverLevel.getBlockState(blockPos);
					if (blockState.is(
							BlockTags.BEEHIVES, blockStateBase -> blockStateBase.hasProperty(BeehiveBlock.HONEY_LEVEL) && blockStateBase.getBlock() instanceof BeehiveBlock
						)
						&& (Integer)blockState.getValue(BeehiveBlock.HONEY_LEVEL) >= 5) {
						((BeehiveBlock)blockState.getBlock())
							.releaseBeesAndResetHoneyLevel(serverLevel, blockState, blockPos, null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
						this.setSuccess(true);
						return this.takeLiquid(blockSource, itemStack, new ItemStack(Items.HONEY_BOTTLE));
					} else if (serverLevel.getFluidState(blockPos).is(FluidTags.WATER)) {
						this.setSuccess(true);
						return this.takeLiquid(blockSource, itemStack, PotionContents.createItemStack(Items.POTION, Potions.WATER));
					} else {
						return super.execute(blockSource, itemStack);
					}
				}
			}
		);
		DispenserBlock.registerBehavior(Items.GLOWSTONE, new OptionalDispenseItemBehavior() {
			@Override
			public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
				BlockPos blockPos = blockSource.pos().relative(direction);
				Level level = blockSource.level();
				BlockState blockState = level.getBlockState(blockPos);
				this.setSuccess(true);
				if (blockState.is(Blocks.RESPAWN_ANCHOR)) {
					if ((Integer)blockState.getValue(RespawnAnchorBlock.CHARGE) != 4) {
						RespawnAnchorBlock.charge(null, level, blockPos, blockState);
						itemStack.shrink(1);
					} else {
						this.setSuccess(false);
					}

					return itemStack;
				} else {
					return super.execute(blockSource, itemStack);
				}
			}
		});
		DispenserBlock.registerBehavior(Items.SHEARS.asItem(), new ShearsDispenseItemBehavior());
		DispenserBlock.registerBehavior(Items.BRUSH.asItem(), new OptionalDispenseItemBehavior() {
			@Override
			protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				ServerLevel serverLevel = blockSource.level();
				BlockPos blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
				List<Armadillo> list = serverLevel.getEntitiesOfClass(Armadillo.class, new AABB(blockPos), EntitySelector.NO_SPECTATORS);
				if (list.isEmpty()) {
					this.setSuccess(false);
					return itemStack;
				} else {
					for (Armadillo armadillo : list) {
						if (armadillo.brushOffScute()) {
							itemStack.hurtAndBreak(16, serverLevel, null, item -> {
							});
							return itemStack;
						}
					}

					this.setSuccess(false);
					return itemStack;
				}
			}
		});
		DispenserBlock.registerBehavior(Items.HONEYCOMB, new OptionalDispenseItemBehavior() {
			@Override
			public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				BlockPos blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
				Level level = blockSource.level();
				BlockState blockState = level.getBlockState(blockPos);
				Optional<BlockState> optional = HoneycombItem.getWaxed(blockState);
				if (optional.isPresent()) {
					level.setBlockAndUpdate(blockPos, (BlockState)optional.get());
					level.levelEvent(3003, blockPos, 0);
					itemStack.shrink(1);
					this.setSuccess(true);
					return itemStack;
				} else {
					return super.execute(blockSource, itemStack);
				}
			}
		});
		DispenserBlock.registerBehavior(
			Items.POTION,
			new DefaultDispenseItemBehavior() {
				private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

				@Override
				public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
					PotionContents potionContents = itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
					if (!potionContents.is(Potions.WATER)) {
						return this.defaultDispenseItemBehavior.dispense(blockSource, itemStack);
					} else {
						ServerLevel serverLevel = blockSource.level();
						BlockPos blockPos = blockSource.pos();
						BlockPos blockPos2 = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
						if (!serverLevel.getBlockState(blockPos2).is(BlockTags.CONVERTABLE_TO_MUD)) {
							return this.defaultDispenseItemBehavior.dispense(blockSource, itemStack);
						} else {
							if (!serverLevel.isClientSide) {
								for (int i = 0; i < 5; i++) {
									serverLevel.sendParticles(
										ParticleTypes.SPLASH,
										(double)blockPos.getX() + serverLevel.random.nextDouble(),
										(double)(blockPos.getY() + 1),
										(double)blockPos.getZ() + serverLevel.random.nextDouble(),
										1,
										0.0,
										0.0,
										0.0,
										1.0
									);
								}
							}

							serverLevel.playSound(null, blockPos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
							serverLevel.gameEvent(null, GameEvent.FLUID_PLACE, blockPos);
							serverLevel.setBlockAndUpdate(blockPos2, Blocks.MUD.defaultBlockState());
							return this.consumeWithRemainder(blockSource, itemStack, new ItemStack(Items.GLASS_BOTTLE));
						}
					}
				}
			}
		);
		DispenserBlock.registerBehavior(Items.MINECART, new MinecartDispenseItemBehavior(EntityType.MINECART));
		DispenserBlock.registerBehavior(Items.CHEST_MINECART, new MinecartDispenseItemBehavior(EntityType.CHEST_MINECART));
		DispenserBlock.registerBehavior(Items.FURNACE_MINECART, new MinecartDispenseItemBehavior(EntityType.FURNACE_MINECART));
		DispenserBlock.registerBehavior(Items.TNT_MINECART, new MinecartDispenseItemBehavior(EntityType.TNT_MINECART));
		DispenserBlock.registerBehavior(Items.HOPPER_MINECART, new MinecartDispenseItemBehavior(EntityType.HOPPER_MINECART));
		DispenserBlock.registerBehavior(Items.COMMAND_BLOCK_MINECART, new MinecartDispenseItemBehavior(EntityType.COMMAND_BLOCK_MINECART));
	}
}
