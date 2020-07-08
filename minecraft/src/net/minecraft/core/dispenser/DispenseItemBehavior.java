package net.minecraft.core.dispenser;

import java.util.List;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;

public interface DispenseItemBehavior {
	DispenseItemBehavior NOOP = (blockSource, itemStack) -> itemStack;

	ItemStack dispense(BlockSource blockSource, ItemStack itemStack);

	static void bootStrap() {
		DispenserBlock.registerBehavior(Items.ARROW, new AbstractProjectileDispenseBehavior() {
			@Override
			protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
				Arrow arrow = new Arrow(level, position.x(), position.y(), position.z());
				arrow.pickup = AbstractArrow.Pickup.ALLOWED;
				return arrow;
			}
		});
		DispenserBlock.registerBehavior(Items.TIPPED_ARROW, new AbstractProjectileDispenseBehavior() {
			@Override
			protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
				Arrow arrow = new Arrow(level, position.x(), position.y(), position.z());
				arrow.setEffectsFromItem(itemStack);
				arrow.pickup = AbstractArrow.Pickup.ALLOWED;
				return arrow;
			}
		});
		DispenserBlock.registerBehavior(Items.SPECTRAL_ARROW, new AbstractProjectileDispenseBehavior() {
			@Override
			protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
				AbstractArrow abstractArrow = new SpectralArrow(level, position.x(), position.y(), position.z());
				abstractArrow.pickup = AbstractArrow.Pickup.ALLOWED;
				return abstractArrow;
			}
		});
		DispenserBlock.registerBehavior(Items.EGG, new AbstractProjectileDispenseBehavior() {
			@Override
			protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
				return Util.make(new ThrownEgg(level, position.x(), position.y(), position.z()), thrownEgg -> thrownEgg.setItem(itemStack));
			}
		});
		DispenserBlock.registerBehavior(Items.SNOWBALL, new AbstractProjectileDispenseBehavior() {
			@Override
			protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
				return Util.make(new Snowball(level, position.x(), position.y(), position.z()), snowball -> snowball.setItem(itemStack));
			}
		});
		DispenserBlock.registerBehavior(
			Items.EXPERIENCE_BOTTLE,
			new AbstractProjectileDispenseBehavior() {
				@Override
				protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
					return Util.make(
						new ThrownExperienceBottle(level, position.x(), position.y(), position.z()), thrownExperienceBottle -> thrownExperienceBottle.setItem(itemStack)
					);
				}

				@Override
				protected float getUncertainty() {
					return super.getUncertainty() * 0.5F;
				}

				@Override
				protected float getPower() {
					return super.getPower() * 1.25F;
				}
			}
		);
		DispenserBlock.registerBehavior(Items.SPLASH_POTION, new DispenseItemBehavior() {
			@Override
			public ItemStack dispense(BlockSource blockSource, ItemStack itemStack) {
				return (new AbstractProjectileDispenseBehavior() {
					@Override
					protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
						return Util.make(new ThrownPotion(level, position.x(), position.y(), position.z()), thrownPotion -> thrownPotion.setItem(itemStack));
					}

					@Override
					protected float getUncertainty() {
						return super.getUncertainty() * 0.5F;
					}

					@Override
					protected float getPower() {
						return super.getPower() * 1.25F;
					}
				}).dispense(blockSource, itemStack);
			}
		});
		DispenserBlock.registerBehavior(Items.LINGERING_POTION, new DispenseItemBehavior() {
			@Override
			public ItemStack dispense(BlockSource blockSource, ItemStack itemStack) {
				return (new AbstractProjectileDispenseBehavior() {
					@Override
					protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
						return Util.make(new ThrownPotion(level, position.x(), position.y(), position.z()), thrownPotion -> thrownPotion.setItem(itemStack));
					}

					@Override
					protected float getUncertainty() {
						return super.getUncertainty() * 0.5F;
					}

					@Override
					protected float getPower() {
						return super.getPower() * 1.25F;
					}
				}).dispense(blockSource, itemStack);
			}
		});
		DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior() {
			@Override
			public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);
				EntityType<?> entityType = ((SpawnEggItem)itemStack.getItem()).getType(itemStack.getTag());
				entityType.spawn(
					blockSource.getLevel(), itemStack, null, blockSource.getPos().relative(direction), MobSpawnType.DISPENSER, direction != Direction.UP, false
				);
				itemStack.shrink(1);
				return itemStack;
			}
		};

		for (SpawnEggItem spawnEggItem : SpawnEggItem.eggs()) {
			DispenserBlock.registerBehavior(spawnEggItem, defaultDispenseItemBehavior);
		}

		DispenserBlock.registerBehavior(Items.ARMOR_STAND, new DefaultDispenseItemBehavior() {
			@Override
			public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);
				BlockPos blockPos = blockSource.getPos().relative(direction);
				Level level = blockSource.getLevel();
				ArmorStand armorStand = new ArmorStand(level, (double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5);
				EntityType.updateCustomEntityTag(level, null, armorStand, itemStack.getTag());
				armorStand.yRot = direction.toYRot();
				level.addFreshEntity(armorStand);
				itemStack.shrink(1);
				return itemStack;
			}
		});
		DispenserBlock.registerBehavior(Items.SADDLE, new OptionalDispenseItemBehavior() {
			@Override
			public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				BlockPos blockPos = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING));
				List<LivingEntity> list = blockSource.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(blockPos), livingEntity -> {
					if (!(livingEntity instanceof Saddleable)) {
						return false;
					} else {
						Saddleable saddleable = (Saddleable)livingEntity;
						return !saddleable.isSaddled() && saddleable.isSaddleable();
					}
				});
				if (!list.isEmpty()) {
					((Saddleable)list.get(0)).equipSaddle(SoundSource.BLOCKS);
					itemStack.shrink(1);
					this.setSuccess(true);
					return itemStack;
				} else {
					return super.execute(blockSource, itemStack);
				}
			}
		});
		DefaultDispenseItemBehavior defaultDispenseItemBehavior2 = new OptionalDispenseItemBehavior() {
			@Override
			protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				BlockPos blockPos = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING));

				for (AbstractHorse abstractHorse : blockSource.getLevel()
					.getEntitiesOfClass(AbstractHorse.class, new AABB(blockPos), abstractHorsex -> abstractHorsex.isAlive() && abstractHorsex.canWearArmor())) {
					if (abstractHorse.isArmor(itemStack) && !abstractHorse.isWearingArmor() && abstractHorse.isTamed()) {
						abstractHorse.setSlot(401, itemStack.split(1));
						this.setSuccess(true);
						return itemStack;
					}
				}

				return super.execute(blockSource, itemStack);
			}
		};
		DispenserBlock.registerBehavior(Items.LEATHER_HORSE_ARMOR, defaultDispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.IRON_HORSE_ARMOR, defaultDispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.GOLDEN_HORSE_ARMOR, defaultDispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.DIAMOND_HORSE_ARMOR, defaultDispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.WHITE_CARPET, defaultDispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.ORANGE_CARPET, defaultDispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.CYAN_CARPET, defaultDispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.BLUE_CARPET, defaultDispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.BROWN_CARPET, defaultDispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.BLACK_CARPET, defaultDispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.GRAY_CARPET, defaultDispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.GREEN_CARPET, defaultDispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.LIGHT_BLUE_CARPET, defaultDispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.LIGHT_GRAY_CARPET, defaultDispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.LIME_CARPET, defaultDispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.MAGENTA_CARPET, defaultDispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.PINK_CARPET, defaultDispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.PURPLE_CARPET, defaultDispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.RED_CARPET, defaultDispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.YELLOW_CARPET, defaultDispenseItemBehavior2);
		DispenserBlock.registerBehavior(
			Items.CHEST,
			new OptionalDispenseItemBehavior() {
				@Override
				public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
					BlockPos blockPos = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING));

					for (AbstractChestedHorse abstractChestedHorse : blockSource.getLevel()
						.getEntitiesOfClass(
							AbstractChestedHorse.class, new AABB(blockPos), abstractChestedHorsex -> abstractChestedHorsex.isAlive() && !abstractChestedHorsex.hasChest()
						)) {
						if (abstractChestedHorse.isTamed() && abstractChestedHorse.setSlot(499, itemStack)) {
							itemStack.shrink(1);
							this.setSuccess(true);
							return itemStack;
						}
					}

					return super.execute(blockSource, itemStack);
				}
			}
		);
		DispenserBlock.registerBehavior(
			Items.FIREWORK_ROCKET,
			new DefaultDispenseItemBehavior() {
				@Override
				public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
					Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);
					FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(
						blockSource.getLevel(), itemStack, blockSource.x(), blockSource.y(), blockSource.x(), true
					);
					DispenseItemBehavior.setEntityPokingOutOfBlock(blockSource, fireworkRocketEntity, direction);
					fireworkRocketEntity.shoot((double)direction.getStepX(), (double)direction.getStepY(), (double)direction.getStepZ(), 0.5F, 1.0F);
					blockSource.getLevel().addFreshEntity(fireworkRocketEntity);
					itemStack.shrink(1);
					return itemStack;
				}

				@Override
				protected void playSound(BlockSource blockSource) {
					blockSource.getLevel().levelEvent(1004, blockSource.getPos(), 0);
				}
			}
		);
		DispenserBlock.registerBehavior(Items.FIRE_CHARGE, new DefaultDispenseItemBehavior() {
			@Override
			public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);
				Position position = DispenserBlock.getDispensePosition(blockSource);
				double d = position.x() + (double)((float)direction.getStepX() * 0.3F);
				double e = position.y() + (double)((float)direction.getStepY() * 0.3F);
				double f = position.z() + (double)((float)direction.getStepZ() * 0.3F);
				Level level = blockSource.getLevel();
				Random random = level.random;
				double g = random.nextGaussian() * 0.05 + (double)direction.getStepX();
				double h = random.nextGaussian() * 0.05 + (double)direction.getStepY();
				double i = random.nextGaussian() * 0.05 + (double)direction.getStepZ();
				level.addFreshEntity(Util.make(new SmallFireball(level, d, e, f, g, h, i), smallFireball -> smallFireball.setItem(itemStack)));
				itemStack.shrink(1);
				return itemStack;
			}

			@Override
			protected void playSound(BlockSource blockSource) {
				blockSource.getLevel().levelEvent(1018, blockSource.getPos(), 0);
			}
		});
		DispenserBlock.registerBehavior(Items.OAK_BOAT, new BoatDispenseItemBehavior(Boat.Type.OAK));
		DispenserBlock.registerBehavior(Items.SPRUCE_BOAT, new BoatDispenseItemBehavior(Boat.Type.SPRUCE));
		DispenserBlock.registerBehavior(Items.BIRCH_BOAT, new BoatDispenseItemBehavior(Boat.Type.BIRCH));
		DispenserBlock.registerBehavior(Items.JUNGLE_BOAT, new BoatDispenseItemBehavior(Boat.Type.JUNGLE));
		DispenserBlock.registerBehavior(Items.DARK_OAK_BOAT, new BoatDispenseItemBehavior(Boat.Type.DARK_OAK));
		DispenserBlock.registerBehavior(Items.ACACIA_BOAT, new BoatDispenseItemBehavior(Boat.Type.ACACIA));
		DispenseItemBehavior dispenseItemBehavior = new DefaultDispenseItemBehavior() {
			private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

			@Override
			public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				BucketItem bucketItem = (BucketItem)itemStack.getItem();
				BlockPos blockPos = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING));
				Level level = blockSource.getLevel();
				if (bucketItem.emptyBucket(null, level, blockPos, null)) {
					bucketItem.checkExtraContent(level, itemStack, blockPos);
					return new ItemStack(Items.BUCKET);
				} else {
					return this.defaultDispenseItemBehavior.dispense(blockSource, itemStack);
				}
			}
		};
		DispenserBlock.registerBehavior(Items.LAVA_BUCKET, dispenseItemBehavior);
		DispenserBlock.registerBehavior(Items.WATER_BUCKET, dispenseItemBehavior);
		DispenserBlock.registerBehavior(Items.SALMON_BUCKET, dispenseItemBehavior);
		DispenserBlock.registerBehavior(Items.COD_BUCKET, dispenseItemBehavior);
		DispenserBlock.registerBehavior(Items.PUFFERFISH_BUCKET, dispenseItemBehavior);
		DispenserBlock.registerBehavior(Items.TROPICAL_FISH_BUCKET, dispenseItemBehavior);
		DispenserBlock.registerBehavior(Items.BUCKET, new DefaultDispenseItemBehavior() {
			private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

			@Override
			public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				LevelAccessor levelAccessor = blockSource.getLevel();
				BlockPos blockPos = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING));
				BlockState blockState = levelAccessor.getBlockState(blockPos);
				Block block = blockState.getBlock();
				if (block instanceof BucketPickup) {
					Fluid fluid = ((BucketPickup)block).takeLiquid(levelAccessor, blockPos, blockState);
					if (!(fluid instanceof FlowingFluid)) {
						return super.execute(blockSource, itemStack);
					} else {
						Item item = fluid.getBucket();
						itemStack.shrink(1);
						if (itemStack.isEmpty()) {
							return new ItemStack(item);
						} else {
							if (blockSource.<DispenserBlockEntity>getEntity().addItem(new ItemStack(item)) < 0) {
								this.defaultDispenseItemBehavior.dispense(blockSource, new ItemStack(item));
							}

							return itemStack;
						}
					}
				} else {
					return super.execute(blockSource, itemStack);
				}
			}
		});
		DispenserBlock.registerBehavior(Items.FLINT_AND_STEEL, new OptionalDispenseItemBehavior() {
			@Override
			protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				Level level = blockSource.getLevel();
				this.setSuccess(true);
				Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);
				BlockPos blockPos = blockSource.getPos().relative(direction);
				BlockState blockState = level.getBlockState(blockPos);
				if (BaseFireBlock.canBePlacedAt(level, blockPos, direction)) {
					level.setBlockAndUpdate(blockPos, BaseFireBlock.getState(level, blockPos));
				} else if (CampfireBlock.canLight(blockState)) {
					level.setBlockAndUpdate(blockPos, blockState.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)));
				} else if (blockState.getBlock() instanceof TntBlock) {
					TntBlock.explode(level, blockPos);
					level.removeBlock(blockPos, false);
				} else {
					this.setSuccess(false);
				}

				if (this.isSuccess() && itemStack.hurt(1, level.random, null)) {
					itemStack.setCount(0);
				}

				return itemStack;
			}
		});
		DispenserBlock.registerBehavior(Items.BONE_MEAL, new OptionalDispenseItemBehavior() {
			@Override
			protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				this.setSuccess(true);
				Level level = blockSource.getLevel();
				BlockPos blockPos = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING));
				if (!BoneMealItem.growCrop(itemStack, level, blockPos) && !BoneMealItem.growWaterPlant(itemStack, level, blockPos, null)) {
					this.setSuccess(false);
				} else if (!level.isClientSide) {
					level.levelEvent(2005, blockPos, 0);
				}

				return itemStack;
			}
		});
		DispenserBlock.registerBehavior(Blocks.TNT, new DefaultDispenseItemBehavior() {
			@Override
			protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				Level level = blockSource.getLevel();
				BlockPos blockPos = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING));
				PrimedTnt primedTnt = new PrimedTnt(level, (double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5, null);
				level.addFreshEntity(primedTnt);
				level.playSound(null, primedTnt.getX(), primedTnt.getY(), primedTnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
				itemStack.shrink(1);
				return itemStack;
			}
		});
		DispenseItemBehavior dispenseItemBehavior2 = new OptionalDispenseItemBehavior() {
			@Override
			protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				this.setSuccess(ArmorItem.dispenseArmor(blockSource, itemStack));
				return itemStack;
			}
		};
		DispenserBlock.registerBehavior(Items.CREEPER_HEAD, dispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.ZOMBIE_HEAD, dispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.DRAGON_HEAD, dispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.SKELETON_SKULL, dispenseItemBehavior2);
		DispenserBlock.registerBehavior(Items.PLAYER_HEAD, dispenseItemBehavior2);
		DispenserBlock.registerBehavior(
			Items.WITHER_SKELETON_SKULL,
			new OptionalDispenseItemBehavior() {
				@Override
				protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
					Level level = blockSource.getLevel();
					Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);
					BlockPos blockPos = blockSource.getPos().relative(direction);
					if (level.isEmptyBlock(blockPos) && WitherSkullBlock.canSpawnMob(level, blockPos, itemStack)) {
						level.setBlock(
							blockPos,
							Blocks.WITHER_SKELETON_SKULL
								.defaultBlockState()
								.setValue(SkullBlock.ROTATION, Integer.valueOf(direction.getAxis() == Direction.Axis.Y ? 0 : direction.getOpposite().get2DDataValue() * 4)),
							3
						);
						BlockEntity blockEntity = level.getBlockEntity(blockPos);
						if (blockEntity instanceof SkullBlockEntity) {
							WitherSkullBlock.checkSpawn(level, blockPos, (SkullBlockEntity)blockEntity);
						}

						itemStack.shrink(1);
						this.setSuccess(true);
					} else {
						this.setSuccess(ArmorItem.dispenseArmor(blockSource, itemStack));
					}

					return itemStack;
				}
			}
		);
		DispenserBlock.registerBehavior(Blocks.CARVED_PUMPKIN, new OptionalDispenseItemBehavior() {
			@Override
			protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				Level level = blockSource.getLevel();
				BlockPos blockPos = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING));
				CarvedPumpkinBlock carvedPumpkinBlock = (CarvedPumpkinBlock)Blocks.CARVED_PUMPKIN;
				if (level.isEmptyBlock(blockPos) && carvedPumpkinBlock.canSpawnGolem(level, blockPos)) {
					if (!level.isClientSide) {
						level.setBlock(blockPos, carvedPumpkinBlock.defaultBlockState(), 3);
					}

					itemStack.shrink(1);
					this.setSuccess(true);
				} else {
					this.setSuccess(ArmorItem.dispenseArmor(blockSource, itemStack));
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
				private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

				private ItemStack takeLiquid(BlockSource blockSource, ItemStack itemStack, ItemStack itemStack2) {
					itemStack.shrink(1);
					if (itemStack.isEmpty()) {
						return itemStack2.copy();
					} else {
						if (blockSource.<DispenserBlockEntity>getEntity().addItem(itemStack2.copy()) < 0) {
							this.defaultDispenseItemBehavior.dispense(blockSource, itemStack2.copy());
						}

						return itemStack;
					}
				}

				@Override
				public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
					this.setSuccess(false);
					ServerLevel serverLevel = blockSource.getLevel();
					BlockPos blockPos = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING));
					BlockState blockState = serverLevel.getBlockState(blockPos);
					if (blockState.is(BlockTags.BEEHIVES, blockStateBase -> blockStateBase.hasProperty(BeehiveBlock.HONEY_LEVEL))
						&& (Integer)blockState.getValue(BeehiveBlock.HONEY_LEVEL) >= 5) {
						((BeehiveBlock)blockState.getBlock())
							.releaseBeesAndResetHoneyLevel(serverLevel, blockState, blockPos, null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
						this.setSuccess(true);
						return this.takeLiquid(blockSource, itemStack, new ItemStack(Items.HONEY_BOTTLE));
					} else if (serverLevel.getFluidState(blockPos).is(FluidTags.WATER)) {
						this.setSuccess(true);
						return this.takeLiquid(blockSource, itemStack, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));
					} else {
						return super.execute(blockSource, itemStack);
					}
				}
			}
		);
		DispenserBlock.registerBehavior(Items.GLOWSTONE, new OptionalDispenseItemBehavior() {
			@Override
			public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
				Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);
				BlockPos blockPos = blockSource.getPos().relative(direction);
				Level level = blockSource.getLevel();
				BlockState blockState = level.getBlockState(blockPos);
				this.setSuccess(true);
				if (blockState.is(Blocks.RESPAWN_ANCHOR)) {
					if ((Integer)blockState.getValue(RespawnAnchorBlock.CHARGE) != 4) {
						RespawnAnchorBlock.charge(level, blockPos, blockState);
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
	}

	static void setEntityPokingOutOfBlock(BlockSource blockSource, Entity entity, Direction direction) {
		entity.setPos(
			blockSource.x() + (double)direction.getStepX() * (0.5000099999997474 - (double)entity.getBbWidth() / 2.0),
			blockSource.y() + (double)direction.getStepY() * (0.5000099999997474 - (double)entity.getBbHeight() / 2.0) - (double)entity.getBbHeight() / 2.0,
			blockSource.z() + (double)direction.getStepZ() * (0.5000099999997474 - (double)entity.getBbWidth() / 2.0)
		);
	}
}
