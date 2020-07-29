package net.minecraft.world.entity.projectile;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FishingHook extends Projectile {
	private final Random syncronizedRandom = new Random();
	private boolean biting;
	private int outOfWaterTime;
	private static final EntityDataAccessor<Integer> DATA_HOOKED_ENTITY = SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_BITING = SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.BOOLEAN);
	private int life;
	private int nibble;
	private int timeUntilLured;
	private int timeUntilHooked;
	private float fishAngle;
	private boolean openWater = true;
	private Entity hookedIn;
	private FishingHook.FishHookState currentState = FishingHook.FishHookState.FLYING;
	private final int luck;
	private final int lureSpeed;

	private FishingHook(Level level, Player player, int i, int j) {
		super(EntityType.FISHING_BOBBER, level);
		this.noCulling = true;
		this.setOwner(player);
		player.fishing = this;
		this.luck = Math.max(0, i);
		this.lureSpeed = Math.max(0, j);
	}

	@Environment(EnvType.CLIENT)
	public FishingHook(Level level, Player player, double d, double e, double f) {
		this(level, player, 0, 0);
		this.setPos(d, e, f);
		this.xo = this.getX();
		this.yo = this.getY();
		this.zo = this.getZ();
	}

	public FishingHook(Player player, Level level, int i, int j) {
		this(level, player, i, j);
		float f = player.xRot;
		float g = player.yRot;
		float h = Mth.cos(-g * (float) (Math.PI / 180.0) - (float) Math.PI);
		float k = Mth.sin(-g * (float) (Math.PI / 180.0) - (float) Math.PI);
		float l = -Mth.cos(-f * (float) (Math.PI / 180.0));
		float m = Mth.sin(-f * (float) (Math.PI / 180.0));
		double d = player.getX() - (double)k * 0.3;
		double e = player.getEyeY();
		double n = player.getZ() - (double)h * 0.3;
		this.moveTo(d, e, n, g, f);
		Vec3 vec3 = new Vec3((double)(-k), (double)Mth.clamp(-(m / l), -5.0F, 5.0F), (double)(-h));
		double o = vec3.length();
		vec3 = vec3.multiply(
			0.6 / o + 0.5 + this.random.nextGaussian() * 0.0045,
			0.6 / o + 0.5 + this.random.nextGaussian() * 0.0045,
			0.6 / o + 0.5 + this.random.nextGaussian() * 0.0045
		);
		this.setDeltaMovement(vec3);
		this.yRot = (float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI);
		this.xRot = (float)(Mth.atan2(vec3.y, (double)Mth.sqrt(getHorizontalDistanceSqr(vec3))) * 180.0F / (float)Math.PI);
		this.yRotO = this.yRot;
		this.xRotO = this.xRot;
	}

	@Override
	protected void defineSynchedData() {
		this.getEntityData().define(DATA_HOOKED_ENTITY, 0);
		this.getEntityData().define(DATA_BITING, false);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (DATA_HOOKED_ENTITY.equals(entityDataAccessor)) {
			int i = this.getEntityData().get(DATA_HOOKED_ENTITY);
			this.hookedIn = i > 0 ? this.level.getEntity(i - 1) : null;
		}

		if (DATA_BITING.equals(entityDataAccessor)) {
			this.biting = this.getEntityData().get(DATA_BITING);
			if (this.biting) {
				this.setDeltaMovement(this.getDeltaMovement().x, (double)(-0.4F * Mth.nextFloat(this.syncronizedRandom, 0.6F, 1.0F)), this.getDeltaMovement().z);
			}
		}

		super.onSyncedDataUpdated(entityDataAccessor);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		double e = 64.0;
		return d < 4096.0;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void lerpTo(double d, double e, double f, float g, float h, int i, boolean bl) {
	}

	@Override
	public void tick() {
		this.syncronizedRandom.setSeed(this.getUUID().getLeastSignificantBits() ^ this.level.getGameTime());
		super.tick();
		Player player = this.getPlayerOwner();
		if (player == null) {
			this.remove();
		} else if (this.level.isClientSide || !this.shouldStopFishing(player)) {
			if (this.onGround) {
				this.life++;
				if (this.life >= 1200) {
					this.remove();
					return;
				}
			} else {
				this.life = 0;
			}

			float f = 0.0F;
			BlockPos blockPos = this.blockPosition();
			FluidState fluidState = this.level.getFluidState(blockPos);
			if (fluidState.is(FluidTags.WATER)) {
				f = fluidState.getHeight(this.level, blockPos);
			}

			boolean bl = f > 0.0F;
			if (this.currentState == FishingHook.FishHookState.FLYING) {
				if (this.hookedIn != null) {
					this.setDeltaMovement(Vec3.ZERO);
					this.currentState = FishingHook.FishHookState.HOOKED_IN_ENTITY;
					return;
				}

				if (bl) {
					this.setDeltaMovement(this.getDeltaMovement().multiply(0.3, 0.2, 0.3));
					this.currentState = FishingHook.FishHookState.BOBBING;
					return;
				}

				this.checkCollision();
			} else {
				if (this.currentState == FishingHook.FishHookState.HOOKED_IN_ENTITY) {
					if (this.hookedIn != null) {
						if (this.hookedIn.removed) {
							this.hookedIn = null;
							this.currentState = FishingHook.FishHookState.FLYING;
						} else {
							this.setPos(this.hookedIn.getX(), this.hookedIn.getY(0.8), this.hookedIn.getZ());
						}
					}

					return;
				}

				if (this.currentState == FishingHook.FishHookState.BOBBING) {
					Vec3 vec3 = this.getDeltaMovement();
					double d = this.getY() + vec3.y - (double)blockPos.getY() - (double)f;
					if (Math.abs(d) < 0.01) {
						d += Math.signum(d) * 0.1;
					}

					this.setDeltaMovement(vec3.x * 0.9, vec3.y - d * (double)this.random.nextFloat() * 0.2, vec3.z * 0.9);
					if (this.nibble <= 0 && this.timeUntilHooked <= 0) {
						this.openWater = true;
					} else {
						this.openWater = this.openWater && this.outOfWaterTime < 10 && this.calculateOpenWater(blockPos);
					}

					if (bl) {
						this.outOfWaterTime = Math.max(0, this.outOfWaterTime - 1);
						if (this.biting) {
							this.setDeltaMovement(
								this.getDeltaMovement().add(0.0, -0.1 * (double)this.syncronizedRandom.nextFloat() * (double)this.syncronizedRandom.nextFloat(), 0.0)
							);
						}

						if (!this.level.isClientSide) {
							this.catchingFish(blockPos);
						}
					} else {
						this.outOfWaterTime = Math.min(10, this.outOfWaterTime + 1);
					}
				}
			}

			if (!fluidState.is(FluidTags.WATER)) {
				this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.03, 0.0));
			}

			this.move(MoverType.SELF, this.getDeltaMovement());
			this.updateRotation();
			if (this.currentState == FishingHook.FishHookState.FLYING && (this.onGround || this.horizontalCollision)) {
				this.setDeltaMovement(Vec3.ZERO);
			}

			double e = 0.92;
			this.setDeltaMovement(this.getDeltaMovement().scale(0.92));
			this.reapplyPosition();
		}
	}

	private boolean shouldStopFishing(Player player) {
		ItemStack itemStack = player.getMainHandItem();
		ItemStack itemStack2 = player.getOffhandItem();
		boolean bl = itemStack.getItem() == Items.FISHING_ROD;
		boolean bl2 = itemStack2.getItem() == Items.FISHING_ROD;
		if (!player.removed && player.isAlive() && (bl || bl2) && !(this.distanceToSqr(player) > 1024.0)) {
			return false;
		} else {
			this.remove();
			return true;
		}
	}

	private void checkCollision() {
		HitResult hitResult = ProjectileUtil.getHitResult(this, this::canHitEntity);
		this.onHit(hitResult);
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return super.canHitEntity(entity) || entity.isAlive() && entity instanceof ItemEntity;
	}

	@Override
	protected void onHitEntity(EntityHitResult entityHitResult) {
		super.onHitEntity(entityHitResult);
		if (!this.level.isClientSide) {
			this.hookedIn = entityHitResult.getEntity();
			this.setHookedEntity();
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult blockHitResult) {
		super.onHitBlock(blockHitResult);
		this.setDeltaMovement(this.getDeltaMovement().normalize().scale(blockHitResult.distanceTo(this)));
	}

	private void setHookedEntity() {
		this.getEntityData().set(DATA_HOOKED_ENTITY, this.hookedIn.getId() + 1);
	}

	private void catchingFish(BlockPos blockPos) {
		ServerLevel serverLevel = (ServerLevel)this.level;
		int i = 1;
		BlockPos blockPos2 = blockPos.above();
		if (this.random.nextFloat() < 0.25F && this.level.isRainingAt(blockPos2)) {
			i++;
		}

		if (this.random.nextFloat() < 0.5F && !this.level.canSeeSky(blockPos2)) {
			i--;
		}

		if (this.nibble > 0) {
			this.nibble--;
			if (this.nibble <= 0) {
				this.timeUntilLured = 0;
				this.timeUntilHooked = 0;
				this.getEntityData().set(DATA_BITING, false);
			}
		} else if (this.timeUntilHooked > 0) {
			this.timeUntilHooked -= i;
			if (this.timeUntilHooked > 0) {
				this.fishAngle = (float)((double)this.fishAngle + this.random.nextGaussian() * 4.0);
				float f = this.fishAngle * (float) (Math.PI / 180.0);
				float g = Mth.sin(f);
				float h = Mth.cos(f);
				double d = this.getX() + (double)(g * (float)this.timeUntilHooked * 0.1F);
				double e = (double)((float)Mth.floor(this.getY()) + 1.0F);
				double j = this.getZ() + (double)(h * (float)this.timeUntilHooked * 0.1F);
				BlockState blockState = serverLevel.getBlockState(new BlockPos(d, e - 1.0, j));
				if (blockState.is(Blocks.WATER)) {
					if (this.random.nextFloat() < 0.15F) {
						serverLevel.sendParticles(ParticleTypes.BUBBLE, d, e - 0.1F, j, 1, (double)g, 0.1, (double)h, 0.0);
					}

					float k = g * 0.04F;
					float l = h * 0.04F;
					serverLevel.sendParticles(ParticleTypes.FISHING, d, e, j, 0, (double)l, 0.01, (double)(-k), 1.0);
					serverLevel.sendParticles(ParticleTypes.FISHING, d, e, j, 0, (double)(-l), 0.01, (double)k, 1.0);
				}
			} else {
				this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
				double m = this.getY() + 0.5;
				serverLevel.sendParticles(
					ParticleTypes.BUBBLE,
					this.getX(),
					m,
					this.getZ(),
					(int)(1.0F + this.getBbWidth() * 20.0F),
					(double)this.getBbWidth(),
					0.0,
					(double)this.getBbWidth(),
					0.2F
				);
				serverLevel.sendParticles(
					ParticleTypes.FISHING,
					this.getX(),
					m,
					this.getZ(),
					(int)(1.0F + this.getBbWidth() * 20.0F),
					(double)this.getBbWidth(),
					0.0,
					(double)this.getBbWidth(),
					0.2F
				);
				this.nibble = Mth.nextInt(this.random, 20, 40);
				this.getEntityData().set(DATA_BITING, true);
			}
		} else if (this.timeUntilLured > 0) {
			this.timeUntilLured -= i;
			float f = 0.15F;
			if (this.timeUntilLured < 20) {
				f = (float)((double)f + (double)(20 - this.timeUntilLured) * 0.05);
			} else if (this.timeUntilLured < 40) {
				f = (float)((double)f + (double)(40 - this.timeUntilLured) * 0.02);
			} else if (this.timeUntilLured < 60) {
				f = (float)((double)f + (double)(60 - this.timeUntilLured) * 0.01);
			}

			if (this.random.nextFloat() < f) {
				float g = Mth.nextFloat(this.random, 0.0F, 360.0F) * (float) (Math.PI / 180.0);
				float h = Mth.nextFloat(this.random, 25.0F, 60.0F);
				double d = this.getX() + (double)(Mth.sin(g) * h * 0.1F);
				double e = (double)((float)Mth.floor(this.getY()) + 1.0F);
				double j = this.getZ() + (double)(Mth.cos(g) * h * 0.1F);
				BlockState blockState = serverLevel.getBlockState(new BlockPos(d, e - 1.0, j));
				if (blockState.is(Blocks.WATER)) {
					serverLevel.sendParticles(ParticleTypes.SPLASH, d, e, j, 2 + this.random.nextInt(2), 0.1F, 0.0, 0.1F, 0.0);
				}
			}

			if (this.timeUntilLured <= 0) {
				this.fishAngle = Mth.nextFloat(this.random, 0.0F, 360.0F);
				this.timeUntilHooked = Mth.nextInt(this.random, 20, 80);
			}
		} else {
			this.timeUntilLured = Mth.nextInt(this.random, 100, 600);
			this.timeUntilLured = this.timeUntilLured - this.lureSpeed * 20 * 5;
		}
	}

	private boolean calculateOpenWater(BlockPos blockPos) {
		FishingHook.OpenWaterType openWaterType = FishingHook.OpenWaterType.INVALID;

		for (int i = -1; i <= 2; i++) {
			FishingHook.OpenWaterType openWaterType2 = this.getOpenWaterTypeForArea(blockPos.offset(-2, i, -2), blockPos.offset(2, i, 2));
			switch (openWaterType2) {
				case INVALID:
					return false;
				case ABOVE_WATER:
					if (openWaterType == FishingHook.OpenWaterType.INVALID) {
						return false;
					}
					break;
				case INSIDE_WATER:
					if (openWaterType == FishingHook.OpenWaterType.ABOVE_WATER) {
						return false;
					}
			}

			openWaterType = openWaterType2;
		}

		return true;
	}

	private FishingHook.OpenWaterType getOpenWaterTypeForArea(BlockPos blockPos, BlockPos blockPos2) {
		return (FishingHook.OpenWaterType)BlockPos.betweenClosedStream(blockPos, blockPos2)
			.map(this::getOpenWaterTypeForBlock)
			.reduce((openWaterType, openWaterType2) -> openWaterType == openWaterType2 ? openWaterType : FishingHook.OpenWaterType.INVALID)
			.orElse(FishingHook.OpenWaterType.INVALID);
	}

	private FishingHook.OpenWaterType getOpenWaterTypeForBlock(BlockPos blockPos) {
		BlockState blockState = this.level.getBlockState(blockPos);
		if (!blockState.isAir() && !blockState.is(Blocks.LILY_PAD)) {
			FluidState fluidState = blockState.getFluidState();
			return fluidState.is(FluidTags.WATER) && fluidState.isSource() && blockState.getCollisionShape(this.level, blockPos).isEmpty()
				? FishingHook.OpenWaterType.INSIDE_WATER
				: FishingHook.OpenWaterType.INVALID;
		} else {
			return FishingHook.OpenWaterType.ABOVE_WATER;
		}
	}

	public boolean isOpenWaterFishing() {
		return this.openWater;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
	}

	public int retrieve(ItemStack itemStack) {
		Player player = this.getPlayerOwner();
		if (!this.level.isClientSide && player != null) {
			int i = 0;
			if (this.hookedIn != null) {
				this.bringInHookedEntity();
				CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)player, itemStack, this, Collections.emptyList());
				this.level.broadcastEntityEvent(this, (byte)31);
				i = this.hookedIn instanceof ItemEntity ? 3 : 5;
			} else if (this.nibble > 0) {
				LootContext.Builder builder = new LootContext.Builder((ServerLevel)this.level)
					.withParameter(LootContextParams.BLOCK_POS, this.blockPosition())
					.withParameter(LootContextParams.TOOL, itemStack)
					.withParameter(LootContextParams.THIS_ENTITY, this)
					.withRandom(this.random)
					.withLuck((float)this.luck + player.getLuck());
				LootTable lootTable = this.level.getServer().getLootTables().get(BuiltInLootTables.FISHING);
				List<ItemStack> list = lootTable.getRandomItems(builder.create(LootContextParamSets.FISHING));
				CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)player, itemStack, this, list);

				for (ItemStack itemStack2 : list) {
					ItemEntity itemEntity = new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(), itemStack2);
					double d = player.getX() - this.getX();
					double e = player.getY() - this.getY();
					double f = player.getZ() - this.getZ();
					double g = 0.1;
					itemEntity.setDeltaMovement(d * 0.1, e * 0.1 + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);
					this.level.addFreshEntity(itemEntity);
					player.level.addFreshEntity(new ExperienceOrb(player.level, player.getX(), player.getY() + 0.5, player.getZ() + 0.5, this.random.nextInt(6) + 1));
					if (itemStack2.getItem().is(ItemTags.FISHES)) {
						player.awardStat(Stats.FISH_CAUGHT, 1);
					}
				}

				i = 1;
			}

			if (this.onGround) {
				i = 2;
			}

			this.remove();
			return i;
		} else {
			return 0;
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handleEntityEvent(byte b) {
		if (b == 31 && this.level.isClientSide && this.hookedIn instanceof Player && ((Player)this.hookedIn).isLocalPlayer()) {
			this.bringInHookedEntity();
		}

		super.handleEntityEvent(b);
	}

	protected void bringInHookedEntity() {
		Entity entity = this.getOwner();
		if (entity != null) {
			Vec3 vec3 = new Vec3(entity.getX() - this.getX(), entity.getY() - this.getY(), entity.getZ() - this.getZ()).scale(0.1);
			this.hookedIn.setDeltaMovement(this.hookedIn.getDeltaMovement().add(vec3));
		}
	}

	@Override
	protected boolean isMovementNoisy() {
		return false;
	}

	@Override
	public void remove() {
		super.remove();
		Player player = this.getPlayerOwner();
		if (player != null) {
			player.fishing = null;
		}
	}

	@Nullable
	public Player getPlayerOwner() {
		Entity entity = this.getOwner();
		return entity instanceof Player ? (Player)entity : null;
	}

	@Nullable
	public Entity getHookedIn() {
		return this.hookedIn;
	}

	@Override
	public boolean canChangeDimensions() {
		return false;
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		Entity entity = this.getOwner();
		return new ClientboundAddEntityPacket(this, entity == null ? this.getId() : entity.getId());
	}

	static enum FishHookState {
		FLYING,
		HOOKED_IN_ENTITY,
		BOBBING;
	}

	static enum OpenWaterType {
		ABOVE_WATER,
		INSIDE_WATER,
		INVALID;
	}
}
