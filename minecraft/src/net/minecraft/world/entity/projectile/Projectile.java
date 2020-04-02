package net.minecraft.world.entity.projectile;

import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class Projectile extends Entity {
	private UUID ownerUUID;
	private int ownerNetworkId;
	private boolean leftOwner;

	Projectile(EntityType<? extends Projectile> entityType, Level level) {
		super(entityType, level);
	}

	public void setOwner(@Nullable Entity entity) {
		if (entity != null) {
			this.ownerUUID = entity.getUUID();
			this.ownerNetworkId = entity.getId();
		}
	}

	@Nullable
	public Entity getOwner() {
		if (this.ownerUUID != null && this.level instanceof ServerLevel) {
			return ((ServerLevel)this.level).getEntity(this.ownerUUID);
		} else {
			return this.ownerNetworkId != 0 ? this.level.getEntity(this.ownerNetworkId) : null;
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		if (this.ownerUUID != null) {
			compoundTag.putUUID("Owner", this.ownerUUID);
		}

		if (this.leftOwner) {
			compoundTag.putBoolean("LeftOwner", true);
		}
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		if (compoundTag.hasUUID("Owner")) {
			this.ownerUUID = compoundTag.getUUID("Owner");
		}

		this.leftOwner = compoundTag.getBoolean("LeftOwner");
	}

	@Override
	public void tick() {
		if (!this.leftOwner) {
			this.leftOwner = this.checkLeftOwner();
		}

		super.tick();
	}

	private boolean checkLeftOwner() {
		Entity entity = this.getOwner();
		if (entity != null) {
			for (Entity entity2 : this.level.getEntities(this, this.getBoundingBox().inflate(1.0), entityx -> !entityx.isSpectator() && entityx.isPickable())) {
				if (entity2.getRootVehicle() == entity.getRootVehicle()) {
					return false;
				}
			}
		}

		return true;
	}

	public void shoot(double d, double e, double f, float g, float h) {
		Vec3 vec3 = new Vec3(d, e, f)
			.normalize()
			.add(this.random.nextGaussian() * 0.0075F * (double)h, this.random.nextGaussian() * 0.0075F * (double)h, this.random.nextGaussian() * 0.0075F * (double)h)
			.scale((double)g);
		this.setDeltaMovement(vec3);
		float i = Mth.sqrt(getHorizontalDistanceSqr(vec3));
		this.yRot = (float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI);
		this.xRot = (float)(Mth.atan2(vec3.y, (double)i) * 180.0F / (float)Math.PI);
		this.yRotO = this.yRot;
		this.xRotO = this.xRot;
	}

	public void shootFromRotation(Entity entity, float f, float g, float h, float i, float j) {
		float k = -Mth.sin(g * (float) (Math.PI / 180.0)) * Mth.cos(f * (float) (Math.PI / 180.0));
		float l = -Mth.sin((f + h) * (float) (Math.PI / 180.0));
		float m = Mth.cos(g * (float) (Math.PI / 180.0)) * Mth.cos(f * (float) (Math.PI / 180.0));
		this.shoot((double)k, (double)l, (double)m, i, j);
		Vec3 vec3 = entity.getDeltaMovement();
		this.setDeltaMovement(this.getDeltaMovement().add(vec3.x, entity.isOnGround() ? 0.0 : vec3.y, vec3.z));
	}

	protected void onHit(HitResult hitResult) {
		HitResult.Type type = hitResult.getType();
		if (type == HitResult.Type.ENTITY) {
			this.onHitEntity((EntityHitResult)hitResult);
		} else if (type == HitResult.Type.BLOCK) {
			this.onHitBlock((BlockHitResult)hitResult);
		}
	}

	protected void onHitEntity(EntityHitResult entityHitResult) {
	}

	protected void onHitBlock(BlockHitResult blockHitResult) {
		BlockState blockState = this.level.getBlockState(blockHitResult.getBlockPos());
		blockState.onProjectileHit(this.level, blockState, blockHitResult, this);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void lerpMotion(double d, double e, double f) {
		this.setDeltaMovement(d, e, f);
		if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
			float g = Mth.sqrt(d * d + f * f);
			this.xRot = (float)(Mth.atan2(e, (double)g) * 180.0F / (float)Math.PI);
			this.yRot = (float)(Mth.atan2(d, f) * 180.0F / (float)Math.PI);
			this.xRotO = this.xRot;
			this.yRotO = this.yRot;
			this.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, this.xRot);
		}
	}

	protected boolean canHitEntity(Entity entity) {
		if (!entity.isSpectator() && entity.isAlive() && entity.isPickable()) {
			Entity entity2 = this.getOwner();
			return entity2 == null || this.leftOwner || !entity2.isPassengerOfSameVehicle(entity);
		} else {
			return false;
		}
	}

	protected void updateRotation() {
		Vec3 vec3 = this.getDeltaMovement();
		float f = Mth.sqrt(getHorizontalDistanceSqr(vec3));
		this.xRot = lerpRotation(this.xRotO, (float)(Mth.atan2(vec3.y, (double)f) * 180.0F / (float)Math.PI));
		this.yRot = lerpRotation(this.yRotO, (float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI));
	}

	protected static float lerpRotation(float f, float g) {
		while (g - f < -180.0F) {
			f -= 360.0F;
		}

		while (g - f >= 180.0F) {
			f += 360.0F;
		}

		return Mth.lerp(0.2F, f, g);
	}
}
