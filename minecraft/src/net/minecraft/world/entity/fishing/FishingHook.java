package net.minecraft.world.entity.fishing;

import java.util.Collections;
import java.util.List;
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
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FishingHook extends Entity {
	private static final EntityDataAccessor<Integer> DATA_HOOKED_ENTITY = SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.INT);
	private boolean inGround;
	private int life;
	private final Player owner;
	private int flightTime;
	private int nibble;
	private int timeUntilLured;
	private int timeUntilHooked;
	private float fishAngle;
	public Entity hookedIn;
	private FishingHook.FishHookState currentState = FishingHook.FishHookState.FLYING;
	private final int luck;
	private final int lureSpeed;

	private FishingHook(Level level, Player player, int i, int j) {
		super(EntityType.FISHING_BOBBER, level);
		this.noCulling = true;
		this.owner = player;
		this.owner.fishing = this;
		this.luck = Math.max(0, i);
		this.lureSpeed = Math.max(0, j);
	}

	@Environment(EnvType.CLIENT)
	public FishingHook(Level level, Player player, double d, double e, double f) {
		this(level, player, 0, 0);
		this.setPos(d, e, f);
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
	}

	public FishingHook(Player player, Level level, int i, int j) {
		this(level, player, i, j);
		float f = this.owner.xRot;
		float g = this.owner.yRot;
		float h = Mth.cos(-g * (float) (Math.PI / 180.0) - (float) Math.PI);
		float k = Mth.sin(-g * (float) (Math.PI / 180.0) - (float) Math.PI);
		float l = -Mth.cos(-f * (float) (Math.PI / 180.0));
		float m = Mth.sin(-f * (float) (Math.PI / 180.0));
		double d = this.owner.x - (double)k * 0.3;
		double e = this.owner.y + (double)this.owner.getEyeHeight();
		double n = this.owner.z - (double)h * 0.3;
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
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (DATA_HOOKED_ENTITY.equals(entityDataAccessor)) {
			int i = this.getEntityData().get(DATA_HOOKED_ENTITY);
			this.hookedIn = i > 0 ? this.level.getEntity(i - 1) : null;
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
		super.tick();
		if (this.owner == null) {
			this.remove();
		} else if (this.level.isClientSide || !this.shouldStopFishing()) {
			if (this.inGround) {
				this.life++;
				if (this.life >= 1200) {
					this.remove();
					return;
				}
			}

			float f = 0.0F;
			BlockPos blockPos = new BlockPos(this);
			FluidState fluidState = this.level.getFluidState(blockPos);
			if (fluidState.is(FluidTags.WATER)) {
				f = fluidState.getHeight(this.level, blockPos);
			}

			if (this.currentState == FishingHook.FishHookState.FLYING) {
				if (this.hookedIn != null) {
					this.setDeltaMovement(Vec3.ZERO);
					this.currentState = FishingHook.FishHookState.HOOKED_IN_ENTITY;
					return;
				}

				if (f > 0.0F) {
					this.setDeltaMovement(this.getDeltaMovement().multiply(0.3, 0.2, 0.3));
					this.currentState = FishingHook.FishHookState.BOBBING;
					return;
				}

				if (!this.level.isClientSide) {
					this.checkCollision();
				}

				if (!this.inGround && !this.onGround && !this.horizontalCollision) {
					this.flightTime++;
				} else {
					this.flightTime = 0;
					this.setDeltaMovement(Vec3.ZERO);
				}
			} else {
				if (this.currentState == FishingHook.FishHookState.HOOKED_IN_ENTITY) {
					if (this.hookedIn != null) {
						if (this.hookedIn.removed) {
							this.hookedIn = null;
							this.currentState = FishingHook.FishHookState.FLYING;
						} else {
							this.x = this.hookedIn.x;
							this.y = this.hookedIn.getBoundingBox().minY + (double)this.hookedIn.getBbHeight() * 0.8;
							this.z = this.hookedIn.z;
							this.setPos(this.x, this.y, this.z);
						}
					}

					return;
				}

				if (this.currentState == FishingHook.FishHookState.BOBBING) {
					Vec3 vec3 = this.getDeltaMovement();
					double d = this.y + vec3.y - (double)blockPos.getY() - (double)f;
					if (Math.abs(d) < 0.01) {
						d += Math.signum(d) * 0.1;
					}

					this.setDeltaMovement(vec3.x * 0.9, vec3.y - d * (double)this.random.nextFloat() * 0.2, vec3.z * 0.9);
					if (!this.level.isClientSide && f > 0.0F) {
						this.catchingFish(blockPos);
					}
				}
			}

			if (!fluidState.is(FluidTags.WATER)) {
				this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.03, 0.0));
			}

			this.move(MoverType.SELF, this.getDeltaMovement());
			this.updateRotation();
			double e = 0.92;
			this.setDeltaMovement(this.getDeltaMovement().scale(0.92));
			this.setPos(this.x, this.y, this.z);
		}
	}

	private boolean shouldStopFishing() {
		ItemStack itemStack = this.owner.getMainHandItem();
		ItemStack itemStack2 = this.owner.getOffhandItem();
		boolean bl = itemStack.getItem() == Items.FISHING_ROD;
		boolean bl2 = itemStack2.getItem() == Items.FISHING_ROD;
		if (!this.owner.removed && this.owner.isAlive() && (bl || bl2) && !(this.distanceToSqr(this.owner) > 1024.0)) {
			return false;
		} else {
			this.remove();
			return true;
		}
	}

	private void updateRotation() {
		Vec3 vec3 = this.getDeltaMovement();
		float f = Mth.sqrt(getHorizontalDistanceSqr(vec3));
		this.yRot = (float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI);
		this.xRot = (float)(Mth.atan2(vec3.y, (double)f) * 180.0F / (float)Math.PI);

		while (this.xRot - this.xRotO < -180.0F) {
			this.xRotO -= 360.0F;
		}

		while (this.xRot - this.xRotO >= 180.0F) {
			this.xRotO += 360.0F;
		}

		while (this.yRot - this.yRotO < -180.0F) {
			this.yRotO -= 360.0F;
		}

		while (this.yRot - this.yRotO >= 180.0F) {
			this.yRotO += 360.0F;
		}

		this.xRot = Mth.lerp(0.2F, this.xRotO, this.xRot);
		this.yRot = Mth.lerp(0.2F, this.yRotO, this.yRot);
	}

	private void checkCollision() {
		HitResult hitResult = ProjectileUtil.getHitResult(
			this,
			this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0),
			entity -> !entity.isSpectator() && (entity.isPickable() || entity instanceof ItemEntity) && (entity != this.owner || this.flightTime >= 5),
			ClipContext.Block.COLLIDER,
			true
		);
		if (hitResult.getType() != HitResult.Type.MISS) {
			if (hitResult.getType() == HitResult.Type.ENTITY) {
				this.hookedIn = ((EntityHitResult)hitResult).getEntity();
				this.setHookedEntity();
			} else {
				this.inGround = true;
			}
		}
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
			} else {
				this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.2 * (double)this.random.nextFloat() * (double)this.random.nextFloat(), 0.0));
			}
		} else if (this.timeUntilHooked > 0) {
			this.timeUntilHooked -= i;
			if (this.timeUntilHooked > 0) {
				this.fishAngle = (float)((double)this.fishAngle + this.random.nextGaussian() * 4.0);
				float f = this.fishAngle * (float) (Math.PI / 180.0);
				float g = Mth.sin(f);
				float h = Mth.cos(f);
				double d = this.x + (double)(g * (float)this.timeUntilHooked * 0.1F);
				double e = (double)((float)Mth.floor(this.getBoundingBox().minY) + 1.0F);
				double j = this.z + (double)(h * (float)this.timeUntilHooked * 0.1F);
				Block block = serverLevel.getBlockState(new BlockPos(d, e - 1.0, j)).getBlock();
				if (block == Blocks.WATER) {
					if (this.random.nextFloat() < 0.15F) {
						serverLevel.sendParticles(ParticleTypes.BUBBLE, d, e - 0.1F, j, 1, (double)g, 0.1, (double)h, 0.0);
					}

					float k = g * 0.04F;
					float l = h * 0.04F;
					serverLevel.sendParticles(ParticleTypes.FISHING, d, e, j, 0, (double)l, 0.01, (double)(-k), 1.0);
					serverLevel.sendParticles(ParticleTypes.FISHING, d, e, j, 0, (double)(-l), 0.01, (double)k, 1.0);
				}
			} else {
				Vec3 vec3 = this.getDeltaMovement();
				this.setDeltaMovement(vec3.x, (double)(-0.4F * Mth.nextFloat(this.random, 0.6F, 1.0F)), vec3.z);
				this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
				double m = this.getBoundingBox().minY + 0.5;
				serverLevel.sendParticles(
					ParticleTypes.BUBBLE, this.x, m, this.z, (int)(1.0F + this.getBbWidth() * 20.0F), (double)this.getBbWidth(), 0.0, (double)this.getBbWidth(), 0.2F
				);
				serverLevel.sendParticles(
					ParticleTypes.FISHING, this.x, m, this.z, (int)(1.0F + this.getBbWidth() * 20.0F), (double)this.getBbWidth(), 0.0, (double)this.getBbWidth(), 0.2F
				);
				this.nibble = Mth.nextInt(this.random, 20, 40);
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
				double d = this.x + (double)(Mth.sin(g) * h * 0.1F);
				double e = (double)((float)Mth.floor(this.getBoundingBox().minY) + 1.0F);
				double j = this.z + (double)(Mth.cos(g) * h * 0.1F);
				Block block = serverLevel.getBlockState(new BlockPos(d, e - 1.0, j)).getBlock();
				if (block == Blocks.WATER) {
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

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
	}

	public int retrieve(ItemStack itemStack) {
		if (!this.level.isClientSide && this.owner != null) {
			int i = 0;
			if (this.hookedIn != null) {
				this.bringInHookedEntity();
				CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)this.owner, itemStack, this, Collections.emptyList());
				this.level.broadcastEntityEvent(this, (byte)31);
				i = this.hookedIn instanceof ItemEntity ? 3 : 5;
			} else if (this.nibble > 0) {
				LootContext.Builder builder = new LootContext.Builder((ServerLevel)this.level)
					.withParameter(LootContextParams.BLOCK_POS, new BlockPos(this))
					.withParameter(LootContextParams.TOOL, itemStack)
					.withRandom(this.random)
					.withLuck((float)this.luck + this.owner.getLuck());
				LootTable lootTable = this.level.getServer().getLootTables().get(BuiltInLootTables.FISHING);
				List<ItemStack> list = lootTable.getRandomItems(builder.create(LootContextParamSets.FISHING));
				CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)this.owner, itemStack, this, list);

				for (ItemStack itemStack2 : list) {
					ItemEntity itemEntity = new ItemEntity(this.level, this.x, this.y, this.z, itemStack2);
					double d = this.owner.x - this.x;
					double e = this.owner.y - this.y;
					double f = this.owner.z - this.z;
					double g = 0.1;
					itemEntity.setDeltaMovement(d * 0.1, e * 0.1 + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);
					this.level.addFreshEntity(itemEntity);
					this.owner.level.addFreshEntity(new ExperienceOrb(this.owner.level, this.owner.x, this.owner.y + 0.5, this.owner.z + 0.5, this.random.nextInt(6) + 1));
					if (itemStack2.getItem().is(ItemTags.FISHES)) {
						this.owner.awardStat(Stats.FISH_CAUGHT, 1);
					}
				}

				i = 1;
			}

			if (this.inGround) {
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
		if (this.owner != null) {
			Vec3 vec3 = new Vec3(this.owner.x - this.x, this.owner.y - this.y, this.owner.z - this.z).scale(0.1);
			this.hookedIn.setDeltaMovement(this.hookedIn.getDeltaMovement().add(vec3));
		}
	}

	@Override
	protected boolean makeStepSound() {
		return false;
	}

	@Override
	public void remove() {
		super.remove();
		if (this.owner != null) {
			this.owner.fishing = null;
		}
	}

	@Nullable
	public Player getOwner() {
		return this.owner;
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
}
