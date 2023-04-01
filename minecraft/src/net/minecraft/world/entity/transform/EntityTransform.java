package net.minecraft.world.entity.transform;

import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

public record EntityTransform(EntityTransformType type, @Nullable Entity entity, @Nullable GameProfile playerSkin) {
	public static final EntityTransform IDENTITY = new EntityTransform(EntityTransformType.IDENTITY, null, null);

	public static EntityTransform get(Entity entity) {
		return entity instanceof LivingEntity livingEntity ? livingEntity.getTransform() : IDENTITY;
	}

	public void copyProperties(LivingEntity livingEntity) {
		if (this.entity != null) {
			this.entity.copyTransformedProperties(livingEntity);
		}
	}

	public float scale() {
		return this.type.scale();
	}

	public double cameraDistance(double d) {
		return (double)Math.max(this.getEffectiveScaleFactor(), 0.5F) * d;
	}

	public float reachDistance(float f) {
		return Mth.clamp(this.getEffectiveScaleFactor(), 1.0F, 10.0F) * f;
	}

	private float getEffectiveScaleFactor() {
		if (this.entity == null) {
			return this.scale();
		} else {
			float f = this.entity.getBbHeight() * this.scale();
			float g = EntityType.PLAYER.getHeight();
			return f / g;
		}
	}

	public EntityDimensions getDimensions(Pose pose, EntityDimensions entityDimensions) {
		return this.entity == null ? entityDimensions.scale(this.scale()) : this.entity.getDimensions(pose).scale(this.scale());
	}

	public float getEyeHeight(Pose pose, float f) {
		return this.entity == null ? f * this.scale() : this.entity.getEyeHeight(pose) * this.scale();
	}

	public float maxUpStep(float f) {
		float g = this.entity != null ? this.entity.maxUpStep() : f;
		float h = Math.max(this.scale(), 1.0F);
		return Math.max(g * h, f);
	}

	public boolean isIdentity() {
		return this.type.isIdentity();
	}

	public boolean canFly() {
		return this.entity != null && this.entity.canTransformFly();
	}

	public boolean canBreatheUnderwater() {
		if (this.entity == null) {
			return false;
		} else {
			return this.entity instanceof LivingEntity livingEntity ? livingEntity.canBreatheUnderwater() : true;
		}
	}

	public boolean canBreatheInAir() {
		return this.entity instanceof LivingEntity livingEntity ? livingEntity.canTransformBreatheInAir() : true;
	}

	public boolean isSensitiveToWater() {
		if (this.entity instanceof LivingEntity livingEntity && livingEntity.isSensitiveToWater()) {
			return true;
		}

		return false;
	}
}
