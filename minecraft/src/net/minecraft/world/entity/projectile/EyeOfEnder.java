package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EyeOfEnder extends Entity implements ItemSupplier {
	private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(EyeOfEnder.class, EntityDataSerializers.ITEM_STACK);
	private double tx;
	private double ty;
	private double tz;
	private int life;
	private boolean surviveAfterDeath;

	public EyeOfEnder(EntityType<? extends EyeOfEnder> entityType, Level level) {
		super(entityType, level);
	}

	public EyeOfEnder(Level level, double d, double e, double f) {
		this(EntityType.EYE_OF_ENDER, level);
		this.setPos(d, e, f);
	}

	public void setItem(ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			this.getEntityData().set(DATA_ITEM_STACK, this.getDefaultItem());
		} else {
			this.getEntityData().set(DATA_ITEM_STACK, itemStack.copyWithCount(1));
		}
	}

	@Override
	public ItemStack getItem() {
		return this.getEntityData().get(DATA_ITEM_STACK);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_ITEM_STACK, this.getDefaultItem());
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		double e = this.getBoundingBox().getSize() * 4.0;
		if (Double.isNaN(e)) {
			e = 4.0;
		}

		e *= 64.0;
		return d < e * e;
	}

	public void signalTo(BlockPos blockPos) {
		double d = (double)blockPos.getX();
		int i = blockPos.getY();
		double e = (double)blockPos.getZ();
		double f = d - this.getX();
		double g = e - this.getZ();
		double h = Math.sqrt(f * f + g * g);
		if (h > 12.0) {
			this.tx = this.getX() + f / h * 12.0;
			this.tz = this.getZ() + g / h * 12.0;
			this.ty = this.getY() + 8.0;
		} else {
			this.tx = d;
			this.ty = (double)i;
			this.tz = e;
		}

		this.life = 0;
		this.surviveAfterDeath = this.random.nextInt(5) > 0;
	}

	@Override
	public void lerpMotion(double d, double e, double f) {
		this.setDeltaMovement(d, e, f);
		if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
			double g = Math.sqrt(d * d + f * f);
			this.setYRot((float)(Mth.atan2(d, f) * 180.0F / (float)Math.PI));
			this.setXRot((float)(Mth.atan2(e, g) * 180.0F / (float)Math.PI));
			this.yRotO = this.getYRot();
			this.xRotO = this.getXRot();
		}
	}

	@Override
	public void tick() {
		super.tick();
		Vec3 vec3 = this.getDeltaMovement();
		double d = this.getX() + vec3.x;
		double e = this.getY() + vec3.y;
		double f = this.getZ() + vec3.z;
		double g = vec3.horizontalDistance();
		this.setXRot(Projectile.lerpRotation(this.xRotO, (float)(Mth.atan2(vec3.y, g) * 180.0F / (float)Math.PI)));
		this.setYRot(Projectile.lerpRotation(this.yRotO, (float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI)));
		if (!this.level().isClientSide) {
			double h = this.tx - d;
			double i = this.tz - f;
			float j = (float)Math.sqrt(h * h + i * i);
			float k = (float)Mth.atan2(i, h);
			double l = Mth.lerp(0.0025, g, (double)j);
			double m = vec3.y;
			if (j < 1.0F) {
				l *= 0.8;
				m *= 0.8;
			}

			int n = this.getY() < this.ty ? 1 : -1;
			vec3 = new Vec3(Math.cos((double)k) * l, m + ((double)n - m) * 0.015F, Math.sin((double)k) * l);
			this.setDeltaMovement(vec3);
		}

		float o = 0.25F;
		if (this.isInWater()) {
			for (int p = 0; p < 4; p++) {
				this.level().addParticle(ParticleTypes.BUBBLE, d - vec3.x * 0.25, e - vec3.y * 0.25, f - vec3.z * 0.25, vec3.x, vec3.y, vec3.z);
			}
		} else {
			this.level()
				.addParticle(
					ParticleTypes.PORTAL,
					d - vec3.x * 0.25 + this.random.nextDouble() * 0.6 - 0.3,
					e - vec3.y * 0.25 - 0.5,
					f - vec3.z * 0.25 + this.random.nextDouble() * 0.6 - 0.3,
					vec3.x,
					vec3.y,
					vec3.z
				);
		}

		if (!this.level().isClientSide) {
			this.setPos(d, e, f);
			this.life++;
			if (this.life > 80 && !this.level().isClientSide) {
				this.playSound(SoundEvents.ENDER_EYE_DEATH, 1.0F, 1.0F);
				this.discard();
				if (this.surviveAfterDeath) {
					this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), this.getItem()));
				} else {
					this.level().levelEvent(2003, this.blockPosition(), 0);
				}
			}
		} else {
			this.setPosRaw(d, e, f);
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		compoundTag.put("Item", this.getItem().save(this.registryAccess()));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		if (compoundTag.contains("Item", 10)) {
			this.setItem((ItemStack)ItemStack.parse(this.registryAccess(), compoundTag.getCompound("Item")).orElse(this.getDefaultItem()));
		} else {
			this.setItem(this.getDefaultItem());
		}
	}

	private ItemStack getDefaultItem() {
		return new ItemStack(Items.ENDER_EYE);
	}

	@Override
	public float getLightLevelDependentMagicValue() {
		return 1.0F;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}
}
