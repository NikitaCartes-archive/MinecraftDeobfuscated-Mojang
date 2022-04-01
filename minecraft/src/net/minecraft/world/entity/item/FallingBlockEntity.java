package net.minecraft.world.entity.item;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ConfiguredStructureTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.CarriedBlocks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.GenericItemBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class FallingBlockEntity extends Entity {
	public static final Vec3i[] OFFSETS = new Vec3i[]{
		new Vec3i(0, 0, 0),
		new Vec3i(1, 0, 0),
		new Vec3i(0, 0, 1),
		new Vec3i(0, 0, -1),
		new Vec3i(-1, 0, 0),
		new Vec3i(1, 0, -1),
		new Vec3i(1, 0, 1),
		new Vec3i(-1, 0, 1),
		new Vec3i(-1, 0, -1)
	};
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Predicate<Entity> HIT_PREDICATE = EntitySelector.NO_SPECTATORS.and(entity -> !(entity instanceof FallingBlockEntity));
	private BlockState blockState = Blocks.SAND.defaultBlockState();
	public int time;
	public boolean dropItem = true;
	private boolean cancelDrop;
	private boolean hurtEntities;
	private int fallDamageMax = 40;
	private float fallDamagePerDistance;
	@Nullable
	public CompoundTag blockData;
	private boolean farFromStart;
	@Nullable
	private Direction craftDirection = null;
	@Nullable
	private Player thrownBy = null;
	private boolean smelted;
	private float spin;
	private float prevSpin;
	protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(FallingBlockEntity.class, EntityDataSerializers.BLOCK_POS);

	public FallingBlockEntity(EntityType<? extends FallingBlockEntity> entityType, Level level) {
		super(entityType, level);
	}

	public FallingBlockEntity(Level level, double d, double e, double f, BlockState blockState) {
		this(EntityType.FALLING_BLOCK, level);
		this.blockState = blockState;
		this.blocksBuilding = true;
		this.setPos(d, e, f);
		this.setDeltaMovement(Vec3.ZERO);
		this.xo = d;
		this.yo = e;
		this.zo = f;
		this.setStartPos(this.blockPosition());
	}

	public FallingBlockEntity(ServerPlayer serverPlayer, BlockState blockState, Direction direction) {
		this(serverPlayer.level, serverPlayer.getX(), serverPlayer.getY() + (double)serverPlayer.getEyeHeight(), serverPlayer.getZ(), blockState);
		this.thrownBy = serverPlayer;
		if (this.blockState.hasProperty(BlockStateProperties.FACING)) {
			this.blockState = this.blockState.setValue(BlockStateProperties.FACING, Direction.getRandom(this.level.random));
		}

		if (this.blockState.is(Blocks.END_PORTAL_FRAME) && this.level instanceof ServerLevel serverLevel) {
			BlockPos blockPos = serverPlayer.blockPosition();
			BlockPos blockPos2 = serverLevel.findNearestMapFeature(ConfiguredStructureTags.EYE_OF_ENDER_LOCATED, blockPos, 100, false);
			if (blockPos2 != null) {
				BlockPos blockPos3 = blockPos2.subtract(blockPos);
				Direction direction2 = Direction.getNearest((float)blockPos3.getX(), (float)blockPos3.getY(), (float)blockPos3.getZ());
				this.blockState = this.blockState.setValue(EndPortalFrameBlock.FACING, direction2);
			}
		}

		this.craftDirection = direction;
	}

	public static FallingBlockEntity fall(Level level, BlockPos blockPos, BlockState blockState) {
		return fall(level, blockPos, blockState, Vec3.ZERO);
	}

	public static FallingBlockEntity fall(Level level, BlockPos blockPos, BlockState blockState, Vec3 vec3) {
		boolean bl = blockState.hasProperty(BlockStateProperties.WATERLOGGED);
		FallingBlockEntity fallingBlockEntity = new FallingBlockEntity(
			level,
			(double)blockPos.getX() + 0.5,
			(double)blockPos.getY(),
			(double)blockPos.getZ() + 0.5,
			bl ? blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false)) : blockState
		);
		fallingBlockEntity.setDeltaMovement(vec3);
		if (vec3.lengthSqr() > 0.0) {
			fallingBlockEntity.craftDirection = Direction.getNearest(vec3.x, 0.0, vec3.z);
		}

		level.setBlock(blockPos, bl ? blockState.getFluidState().createLegacyBlock() : Blocks.AIR.defaultBlockState(), 3);
		level.addFreshEntity(fallingBlockEntity);
		return fallingBlockEntity;
	}

	@Override
	public boolean isAttackable() {
		return true;
	}

	public void setStartPos(BlockPos blockPos) {
		this.entityData.set(DATA_START_POS, blockPos);
	}

	public BlockPos getStartPos() {
		return this.entityData.get(DATA_START_POS);
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(DATA_START_POS, BlockPos.ZERO);
	}

	@Override
	public boolean isPickable() {
		return !this.isRemoved();
	}

	public float getSpin(float f) {
		return Mth.lerp(f, this.prevSpin, this.spin);
	}

	@Override
	public void tick() {
		if (this.blockState.isAir()) {
			this.discard();
		} else {
			Block block = this.blockState.getBlock();
			if (!this.isNoGravity()) {
				this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
			}

			this.move(MoverType.SELF, this.getDeltaMovement());
			Vec3 vec3 = this.position().subtract(this.xo, this.yo, this.zo);
			this.prevSpin = this.spin;
			this.spin = (float)((double)this.spin + vec3.length() * 50.0);
			double d = this.getDeltaMovement().y < 0.0 ? 3.0 : 2.0;
			if (!this.farFromStart && !this.getStartPos().closerThan(new BlockPos(this.position()), d)) {
				this.farFromStart = true;
			}

			if (this.farFromStart && !this.level.isClientSide()) {
				List<Entity> list = this.level.getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()), HIT_PREDICATE);
				list.removeIf(entity -> !this.hitEntity(entity));
				if (!list.isEmpty()) {
					this.setDeltaMovement(this.getDeltaMovement().reverse().scale(0.1));
					this.hasImpulse = true;
				}
			}

			this.time++;
			if (!this.level.isClientSide) {
				BlockPos blockPos = this.blockPosition();
				FluidState fluidState = this.level.getFluidState(blockPos);
				boolean bl = this.blockState.getBlock() instanceof ConcretePowderBlock;
				boolean bl2 = bl && fluidState.is(FluidTags.WATER);
				double e = this.getDeltaMovement().lengthSqr();
				if (bl && e > 1.0) {
					BlockHitResult blockHitResult = this.level
						.clip(new ClipContext(new Vec3(this.xo, this.yo, this.zo), this.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this));
					if (blockHitResult.getType() != HitResult.Type.MISS && this.level.getFluidState(blockHitResult.getBlockPos()).is(FluidTags.WATER)) {
						blockPos = blockHitResult.getBlockPos();
						bl2 = true;
					}
				}

				if (!this.smelted && fluidState.is(FluidTags.LAVA)) {
					BlockState blockState = this.trySmelt(this.blockState);
					if (blockState != null) {
						this.blockState = blockState;
						this.smelted = true;
					}
				}

				if (this.onGround || bl2) {
					BlockState blockState = this.level.getBlockState(blockPos);
					if (!blockState.isAir() && !blockState.isCollisionShapeFullBlock(this.level, blockPos) && this.getY() - (double)blockPos.getY() > 0.8) {
						blockPos = blockPos.above();
						blockState = this.level.getBlockState(blockPos);
					}

					this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));
					if (!blockState.is(Blocks.MOVING_PISTON) && !this.cancelDrop) {
						boolean bl3 = blockState.canBeReplaced(new DirectionalPlaceContext(this.level, blockPos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
						boolean bl4 = FallingBlock.isFree(this.level.getBlockState(blockPos.below())) && (!bl || !bl2);
						boolean bl5 = this.blockState.canSurvive(this.level, blockPos) && !bl4;
						if (bl3 && !bl4) {
							if (!bl5 && !this.blockState.is(Blocks.GENERIC_ITEM_BLOCK)) {
								BlockState blockState2 = GenericItemBlock.wrap(this.blockState);
								if (blockState2 != null && blockState2.canSurvive(this.level, blockPos)) {
									this.blockState = blockState2;
									bl5 = true;
								}
							} else if (this.blockState.is(Blocks.GENERIC_ITEM_BLOCK)) {
								BlockState blockState2 = GenericItemBlock.unwrap(this.blockState);
								if (blockState2 != null && blockState2.canSurvive(this.level, blockPos)) {
									this.blockState = blockState2;
								}
							}
						}

						if (bl3 && bl5) {
							if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED) && fluidState.getType() == Fluids.WATER) {
								this.blockState = this.blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true));
							}

							if (block instanceof BedBlock) {
								BlockPos blockPos2 = blockPos.relative(BedBlock.getConnectedDirection(this.blockState));
								if (this.level.getBlockState(blockPos2).isAir()) {
									BedPart bedPart = this.blockState.getValue(BedBlock.PART);

									bedPart = switch (bedPart) {
										case HEAD -> BedPart.FOOT;
										case FOOT -> BedPart.HEAD;
									};
									this.level.setBlock(blockPos2, this.blockState.setValue(BedBlock.PART, bedPart), 3);
								}
							}

							BlockState blockState2 = Block.updateFromNeighbourShapes(this.blockState, this.level, blockPos);
							if (this.level.setBlockAndUpdate(blockPos, blockState2)) {
								((ServerLevel)this.level).getChunkSource().chunkMap.broadcast(this, new ClientboundBlockUpdatePacket(blockPos, this.level.getBlockState(blockPos)));
								if (this.thrownBy != null && this.blockState.is(BlockTags.FRAGILE) && this.level.random.nextBoolean()) {
									this.level.destroyBlock(blockPos, false, this.thrownBy);
									((ServerLevel)this.level).getChunkSource().chunkMap.broadcast(this, new ClientboundBlockUpdatePacket(blockPos, this.level.getBlockState(blockPos)));
									this.discard();
									return;
								}

								this.discard();
								if (block instanceof Fallable) {
									((Fallable)block).onLand(this.level, blockPos, this.blockState, blockState, this);
								}

								if (this.blockData != null && this.blockState.hasBlockEntity()) {
									BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
									if (blockEntity != null) {
										CompoundTag compoundTag = blockEntity.saveWithoutMetadata();

										for (String string : this.blockData.getAllKeys()) {
											compoundTag.put(string, this.blockData.get(string).copy());
										}

										try {
											blockEntity.load(compoundTag);
										} catch (Exception var25) {
											LOGGER.error("Failed to load block entity from falling block", (Throwable)var25);
										}

										blockEntity.setChanged();
									}
								}

								boolean bl6 = false;
								if (!this.smelted && this.level.getBlockState(blockPos.below()).is(Blocks.CRAFTING_TABLE)) {
									this.craftDirection = this.craftDirection == null ? Direction.Plane.HORIZONTAL.getRandomDirection(this.level.random) : this.craftDirection;

									for (Vec3i vec3i : OFFSETS) {
										if (this.tryCraft(blockPos.offset(vec3i), this.craftDirection)) {
											bl6 = true;
											break;
										}
									}
								}

								if (!bl6 && this.blockState.is(Blocks.CRAFTING_TABLE)) {
									BlockPos blockPos3 = blockPos.below();
									this.craftDirection = this.craftDirection == null ? Direction.Plane.HORIZONTAL.getRandomDirection(this.level.random) : this.craftDirection;

									for (Vec3i vec3i2 : OFFSETS) {
										if (this.tryCraft(blockPos3.offset(vec3i2), this.craftDirection)) {
											bl6 = true;
											break;
										}
									}

									if (!bl6) {
										for (Direction direction : Direction.Plane.HORIZONTAL) {
											BlockPos blockPos4 = blockPos3.relative(direction);
											if (this.level.getBlockState(blockPos4).isAir()) {
												for (Vec3i vec3i3 : OFFSETS) {
													if (this.tryCraft(blockPos4.offset(vec3i3), this.craftDirection)) {
														bl6 = true;
														break;
													}
												}
											}

											if (bl6) {
												break;
											}
										}
									}
								}
							}
						}
					}
				} else if (!this.level.isClientSide
					&& (this.time > 100 && (blockPos.getY() <= this.level.getMinBuildHeight() || blockPos.getY() > this.level.getMaxBuildHeight()) || this.time > 600)) {
					if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
						this.spawnAtLocation(block);
					}

					this.discard();
				}
			}

			this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
		}
	}

	@Nullable
	private BlockState trySmelt(BlockState blockState) {
		ItemStack itemStack = CarriedBlocks.getItemStackFromBlock(blockState);
		if (itemStack.isEmpty()) {
			return null;
		} else {
			SimpleContainer simpleContainer = new SimpleContainer(itemStack);
			Optional<SmeltingRecipe> optional = this.level.getServer().getRecipeManager().getRecipeFor(RecipeType.SMELTING, simpleContainer, this.level);
			return !optional.isEmpty() && ((SmeltingRecipe)optional.get()).getResultItem().getCount() <= 1
				? (BlockState)CarriedBlocks.getBlockFromItemStack(((SmeltingRecipe)optional.get()).getResultItem()).orElse(null)
				: null;
		}
	}

	private boolean tryCraft(BlockPos blockPos, Direction direction) {
		Vec3i vec3i = direction.getOpposite().getNormal();
		Vec3i vec3i2 = direction.getOpposite().getClockWise().getNormal();
		int i = 0;
		CraftingContainer craftingContainer = new CraftingContainer(null, 3, 3);

		for (int j = -1; j <= 1; j++) {
			for (int k = -1; k <= 1; k++) {
				BlockPos blockPos2 = blockPos.offset(vec3i.getX() * j + vec3i2.getX() * k, 0, vec3i.getZ() * j + vec3i2.getZ() * k);
				BlockState blockState = this.level.getBlockState(blockPos2);
				ItemStack itemStack = CarriedBlocks.getItemStackFromBlock(blockState);
				if (!itemStack.isEmpty()) {
					craftingContainer.setItem(i, itemStack);
				}

				i++;
			}
		}

		Optional<CraftingRecipe> optional = this.level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingContainer, this.level);
		Optional<BlockState> optional2 = optional.flatMap(craftingRecipe -> CarriedBlocks.getBlockFromItemStack(craftingRecipe.getResultItem()));
		optional2.ifPresent(blockStatex -> {
			for (BlockPos blockPos2x : BlockPos.betweenClosed(blockPos.offset(-1, 0, -1), blockPos.offset(1, 0, 1))) {
				if (!CarriedBlocks.getItemStackFromBlock(this.level.getBlockState(blockPos2x)).isEmpty()) {
					this.level.setBlock(blockPos2x, blockStatex, 3);
				}
			}
		});
		return optional.isPresent();
	}

	private boolean hitEntity(Entity entity) {
		DamageSource damageSource = this.thrownBy != null ? DamageSource.thrown(this, this.thrownBy) : DamageSource.FALLING_BLOCK;
		if (this.getDeltaMovement().lengthSqr() > 0.125) {
			if (entity instanceof Skeleton skeleton && CarriedBlocks.getItemStackFromBlock(this.blockState).is(Items.SPYGLASS)) {
				skeleton.hurt(damageSource, 0.0F);
				if (skeleton.getSpyglassesInSockets() < 2) {
					skeleton.addSpyglassIntoEyeSocket();
				}

				this.remove(Entity.RemovalReason.KILLED);
				return true;
			}

			if (entity instanceof LivingEntity livingEntity
				&& livingEntity.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
				&& (this.blockState.is(Blocks.CARVED_PUMPKIN) || this.blockState.is(Blocks.BARREL) || entity instanceof Player)) {
				livingEntity.hurt(damageSource, entity instanceof Player ? 0.125F : 0.0F);
				ItemStack itemStack = CarriedBlocks.getItemStackFromBlock(this.blockState);
				livingEntity.setItemSlot(EquipmentSlot.HEAD, itemStack);
				if (this.blockState.is(Blocks.BARREL)) {
					this.level.playSound(null, entity, SoundEvents.BARREL_OPEN, SoundSource.PLAYERS, 1.0F, 1.0F);
				}

				this.remove(Entity.RemovalReason.KILLED);
				return true;
			}
		}

		if ((this.blockState.is(Blocks.CACTUS) || this.blockState.is(Blocks.POINTED_DRIPSTONE)) && entity instanceof Sheep sheep) {
			if (sheep.readyForShearing()) {
				sheep.shear(SoundSource.NEUTRAL);
			}
		} else if (this.blockState.is(BlockTags.WOOL) && entity instanceof Sheep sheep2) {
			sheep2.hurt(damageSource, 0.0F);
			if (sheep2.readyForShearing()) {
				sheep2.shear(SoundSource.NEUTRAL);
			}

			if (this.blockState.is(Blocks.WHITE_WOOL)) {
				sheep2.setColor(DyeColor.WHITE);
			} else if (this.blockState.is(Blocks.ORANGE_WOOL)) {
				sheep2.setColor(DyeColor.ORANGE);
			} else if (this.blockState.is(Blocks.MAGENTA_WOOL)) {
				sheep2.setColor(DyeColor.MAGENTA);
			} else if (this.blockState.is(Blocks.LIGHT_BLUE_WOOL)) {
				sheep2.setColor(DyeColor.LIGHT_BLUE);
			} else if (this.blockState.is(Blocks.YELLOW_WOOL)) {
				sheep2.setColor(DyeColor.YELLOW);
			} else if (this.blockState.is(Blocks.LIME_WOOL)) {
				sheep2.setColor(DyeColor.LIME);
			} else if (this.blockState.is(Blocks.PINK_WOOL)) {
				sheep2.setColor(DyeColor.PINK);
			} else if (this.blockState.is(Blocks.GRAY_WOOL)) {
				sheep2.setColor(DyeColor.GRAY);
			} else if (this.blockState.is(Blocks.LIGHT_GRAY_WOOL)) {
				sheep2.setColor(DyeColor.LIGHT_GRAY);
			} else if (this.blockState.is(Blocks.CYAN_WOOL)) {
				sheep2.setColor(DyeColor.CYAN);
			} else if (this.blockState.is(Blocks.PURPLE_WOOL)) {
				sheep2.setColor(DyeColor.PURPLE);
			} else if (this.blockState.is(Blocks.BLUE_WOOL)) {
				sheep2.setColor(DyeColor.BLUE);
			} else if (this.blockState.is(Blocks.BROWN_WOOL)) {
				sheep2.setColor(DyeColor.BROWN);
			} else if (this.blockState.is(Blocks.GREEN_WOOL)) {
				sheep2.setColor(DyeColor.GREEN);
			} else if (this.blockState.is(Blocks.RED_WOOL)) {
				sheep2.setColor(DyeColor.RED);
			} else if (this.blockState.is(Blocks.BLACK_WOOL)) {
				sheep2.setColor(DyeColor.BLACK);
			}

			sheep2.setSheared(false);
			this.remove(Entity.RemovalReason.KILLED);
			return true;
		}

		float f = 10.0F;
		float g = this.blockState.getBlock().defaultDestroyTime();
		float h = (float)Math.ceil(this.getDeltaMovement().length() * 10.0 * (double)g);
		if (h > 0.0F && (entity instanceof LivingEntity || entity instanceof EndCrystal)) {
			entity.hurt(damageSource, h);
			entity.setDeltaMovement(
				entity.getDeltaMovement().add(this.getDeltaMovement().scale(0.5 * (double)Mth.sqrt(this.blockState.getBlock().getExplosionResistance())))
			);
			if (entity.isOnGround()) {
				entity.setDeltaMovement(entity.getDeltaMovement().add(0.0, 0.5, 0.0));
			}

			return true;
		} else {
			return false;
		}
	}

	public void callOnBrokenAfterFall(Block block, BlockPos blockPos) {
		if (block instanceof Fallable) {
			((Fallable)block).onBrokenAfterFall(this.level, blockPos, this);
		}
	}

	@Override
	public boolean causeFallDamage(float f, float g, DamageSource damageSource) {
		if (!this.hurtEntities) {
			return false;
		} else {
			int i = Mth.ceil(f - 1.0F);
			if (i < 0) {
				return false;
			} else {
				Predicate<Entity> predicate;
				DamageSource damageSource2;
				if (this.blockState.getBlock() instanceof Fallable) {
					Fallable fallable = (Fallable)this.blockState.getBlock();
					predicate = fallable.getHurtsEntitySelector();
					damageSource2 = fallable.getFallDamageSource();
				} else {
					predicate = EntitySelector.NO_SPECTATORS;
					damageSource2 = DamageSource.FALLING_BLOCK;
				}

				float h = (float)Math.min(Mth.floor((float)i * this.fallDamagePerDistance), this.fallDamageMax);
				this.level.getEntities(this, this.getBoundingBox(), predicate).forEach(entity -> entity.hurt(damageSource2, h));
				boolean bl = this.blockState.is(BlockTags.ANVIL);
				if (bl && h > 0.0F && this.random.nextFloat() < 0.05F + (float)i * 0.05F) {
					BlockState blockState = AnvilBlock.damage(this.blockState);
					if (blockState == null) {
						this.cancelDrop = true;
					} else {
						this.blockState = blockState;
					}
				}

				return false;
			}
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		compoundTag.put("BlockState", NbtUtils.writeBlockState(this.blockState));
		compoundTag.putInt("Time", this.time);
		compoundTag.putBoolean("DropItem", this.dropItem);
		compoundTag.putBoolean("HurtEntities", this.hurtEntities);
		compoundTag.putFloat("FallHurtAmount", this.fallDamagePerDistance);
		compoundTag.putInt("FallHurtMax", this.fallDamageMax);
		if (this.blockData != null) {
			compoundTag.put("TileEntityData", this.blockData);
		}
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		this.blockState = NbtUtils.readBlockState(compoundTag.getCompound("BlockState"));
		this.time = compoundTag.getInt("Time");
		if (compoundTag.contains("HurtEntities", 99)) {
			this.hurtEntities = compoundTag.getBoolean("HurtEntities");
			this.fallDamagePerDistance = compoundTag.getFloat("FallHurtAmount");
			this.fallDamageMax = compoundTag.getInt("FallHurtMax");
		} else if (this.blockState.is(BlockTags.ANVIL)) {
			this.hurtEntities = true;
		}

		if (compoundTag.contains("DropItem", 99)) {
			this.dropItem = compoundTag.getBoolean("DropItem");
		}

		if (compoundTag.contains("TileEntityData", 10)) {
			this.blockData = compoundTag.getCompound("TileEntityData");
		}

		if (this.blockState.isAir()) {
			this.blockState = Blocks.SAND.defaultBlockState();
		}
	}

	public void setHurtsEntities(float f, int i) {
		this.hurtEntities = true;
		this.fallDamagePerDistance = f;
		this.fallDamageMax = i;
	}

	@Override
	public boolean displayFireAnimation() {
		return false;
	}

	@Override
	public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
		super.fillCrashReportCategory(crashReportCategory);
		crashReportCategory.setDetail("Immitating BlockState", this.blockState.toString());
	}

	public BlockState getBlockState() {
		return this.blockState;
	}

	@Override
	public boolean onlyOpCanSetNbt() {
		return true;
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this, Block.getId(this.getBlockState()));
	}

	@Override
	public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
		super.recreateFromPacket(clientboundAddEntityPacket);
		this.blockState = Block.stateById(clientboundAddEntityPacket.getData());
		this.blocksBuilding = true;
		double d = clientboundAddEntityPacket.getX();
		double e = clientboundAddEntityPacket.getY();
		double f = clientboundAddEntityPacket.getZ();
		this.setPos(d, e, f);
		this.setStartPos(this.blockPosition());
	}

	@Override
	public boolean isPushable() {
		return false;
	}
}
