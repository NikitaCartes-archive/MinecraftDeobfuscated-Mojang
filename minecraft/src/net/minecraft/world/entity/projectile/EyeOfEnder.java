package net.minecraft.world.entity.projectile;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvironmentInterface;
import net.fabricmc.api.EnvironmentInterfaces;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
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

@EnvironmentInterfaces({@EnvironmentInterface(
		value = EnvType.CLIENT,
		itf = ItemSupplier.class
	)})
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
		this.life = 0;
		this.setPos(d, e, f);
	}

	public void setItem(ItemStack itemStack) {
		if (itemStack.getItem() != Items.ENDER_EYE || itemStack.hasTag()) {
			this.getEntityData().set(DATA_ITEM_STACK, Util.make(itemStack.copy(), itemStackx -> itemStackx.setCount(1)));
		}
	}

	private ItemStack getItemRaw() {
		return this.getEntityData().get(DATA_ITEM_STACK);
	}

	@Override
	public ItemStack getItem() {
		ItemStack itemStack = this.getItemRaw();
		return itemStack.isEmpty() ? new ItemStack(Items.ENDER_EYE) : itemStack;
	}

	@Override
	protected void defineSynchedData() {
		this.getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
	}

	@Environment(EnvType.CLIENT)
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
		double f = d - this.x;
		double g = e - this.z;
		float h = Mth.sqrt(f * f + g * g);
		if (h > 12.0F) {
			this.tx = this.x + f / (double)h * 12.0;
			this.tz = this.z + g / (double)h * 12.0;
			this.ty = this.y + 8.0;
		} else {
			this.tx = d;
			this.ty = (double)i;
			this.tz = e;
		}

		this.life = 0;
		this.surviveAfterDeath = this.random.nextInt(5) > 0;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void lerpMotion(double d, double e, double f) {
		this.setDeltaMovement(d, e, f);
		if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
			float g = Mth.sqrt(d * d + f * f);
			this.yRot = (float)(Mth.atan2(d, f) * 180.0F / (float)Math.PI);
			this.xRot = (float)(Mth.atan2(e, (double)g) * 180.0F / (float)Math.PI);
			this.yRotO = this.yRot;
			this.xRotO = this.xRot;
		}
	}

	@Override
	public void tick() {
		this.xOld = this.x;
		this.yOld = this.y;
		this.zOld = this.z;
		super.tick();
		Vec3 vec3 = this.getDeltaMovement();
		this.x = this.x + vec3.x;
		this.y = this.y + vec3.y;
		this.z = this.z + vec3.z;
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
		if (!this.level.isClientSide) {
			double d = this.tx - this.x;
			double e = this.tz - this.z;
			float g = (float)Math.sqrt(d * d + e * e);
			float h = (float)Mth.atan2(e, d);
			double i = Mth.lerp(0.0025, (double)f, (double)g);
			double j = vec3.y;
			if (g < 1.0F) {
				i *= 0.8;
				j *= 0.8;
			}

			int k = this.y < this.ty ? 1 : -1;
			vec3 = new Vec3(Math.cos((double)h) * i, j + ((double)k - j) * 0.015F, Math.sin((double)h) * i);
			this.setDeltaMovement(vec3);
		}

		float l = 0.25F;
		if (this.isInWater()) {
			for (int m = 0; m < 4; m++) {
				this.level.addParticle(ParticleTypes.BUBBLE, this.x - vec3.x * 0.25, this.y - vec3.y * 0.25, this.z - vec3.z * 0.25, vec3.x, vec3.y, vec3.z);
			}
		} else {
			this.level
				.addParticle(
					ParticleTypes.PORTAL,
					this.x - vec3.x * 0.25 + this.random.nextDouble() * 0.6 - 0.3,
					this.y - vec3.y * 0.25 - 0.5,
					this.z - vec3.z * 0.25 + this.random.nextDouble() * 0.6 - 0.3,
					vec3.x,
					vec3.y,
					vec3.z
				);
		}

		if (!this.level.isClientSide) {
			this.setPos(this.x, this.y, this.z);
			this.life++;
			if (this.life > 80 && !this.level.isClientSide) {
				this.playSound(SoundEvents.ENDER_EYE_DEATH, 1.0F, 1.0F);
				this.remove();
				if (this.surviveAfterDeath) {
					this.level.addFreshEntity(new ItemEntity(this.level, this.x, this.y, this.z, this.getItem()));
				} else {
					this.level.levelEvent(2003, new BlockPos(this), 0);
				}
			}
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		ItemStack itemStack = this.getItemRaw();
		if (!itemStack.isEmpty()) {
			compoundTag.put("Item", itemStack.save(new CompoundTag()));
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		ItemStack itemStack = ItemStack.of(compoundTag.getCompound("Item"));
		this.setItem(itemStack);
	}

	@Override
	public float getBrightness() {
		return 1.0F;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public int getLightColor() {
		return 15728880;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this);
	}
}
