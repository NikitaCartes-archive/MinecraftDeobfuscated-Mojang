package net.minecraft.world.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ElytraAnimationState {
	private static final float DEFAULT_X_ROT = (float) (Math.PI / 12);
	private static final float DEFAULT_Z_ROT = (float) (-Math.PI / 12);
	private float rotX;
	private float rotY;
	private float rotZ;
	private float rotXOld;
	private float rotYOld;
	private float rotZOld;
	private final LivingEntity entity;

	public ElytraAnimationState(LivingEntity livingEntity) {
		this.entity = livingEntity;
	}

	public void tick() {
		this.rotXOld = this.rotX;
		this.rotYOld = this.rotY;
		this.rotZOld = this.rotZ;
		float g;
		float h;
		float i;
		if (this.entity.isFallFlying()) {
			float f = 1.0F;
			Vec3 vec3 = this.entity.getDeltaMovement();
			if (vec3.y < 0.0) {
				Vec3 vec32 = vec3.normalize();
				f = 1.0F - (float)Math.pow(-vec32.y, 1.5);
			}

			g = Mth.lerp(f, (float) (Math.PI / 12), (float) (Math.PI / 9));
			h = Mth.lerp(f, (float) (-Math.PI / 12), (float) (-Math.PI / 2));
			i = 0.0F;
		} else if (this.entity.isCrouching()) {
			g = (float) (Math.PI * 2.0 / 9.0);
			h = (float) (-Math.PI / 4);
			i = 0.08726646F;
		} else {
			g = (float) (Math.PI / 12);
			h = (float) (-Math.PI / 12);
			i = 0.0F;
		}

		this.rotX = this.rotX + (g - this.rotX) * 0.3F;
		this.rotY = this.rotY + (i - this.rotY) * 0.3F;
		this.rotZ = this.rotZ + (h - this.rotZ) * 0.3F;
	}

	public float getRotX(float f) {
		return Mth.lerp(f, this.rotXOld, this.rotX);
	}

	public float getRotY(float f) {
		return Mth.lerp(f, this.rotYOld, this.rotY);
	}

	public float getRotZ(float f) {
		return Mth.lerp(f, this.rotZOld, this.rotZ);
	}
}
