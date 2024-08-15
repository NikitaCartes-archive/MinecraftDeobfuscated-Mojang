package net.minecraft.world.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class ElytraAnimationState {
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
		if (this.entity.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
			float f = (float) (Math.PI / 12);
			float g = (float) (-Math.PI / 12);
			float h = 0.0F;
			if (this.entity.isFallFlying()) {
				float i = 1.0F;
				Vec3 vec3 = this.entity.getDeltaMovement();
				if (vec3.y < 0.0) {
					Vec3 vec32 = vec3.normalize();
					i = 1.0F - (float)Math.pow(-vec32.y, 1.5);
				}

				f = i * (float) (Math.PI / 9) + (1.0F - i) * f;
				g = i * (float) (-Math.PI / 2) + (1.0F - i) * g;
			} else if (this.entity.isCrouching()) {
				f = (float) (Math.PI * 2.0 / 9.0);
				g = (float) (-Math.PI / 4);
				h = 0.08726646F;
			}

			this.rotX = this.rotX + (f - this.rotX) * 0.3F;
			this.rotY = this.rotY + (h - this.rotY) * 0.3F;
			this.rotZ = this.rotZ + (g - this.rotZ) * 0.3F;
		} else {
			this.rotX = 0.0F;
			this.rotY = 0.0F;
			this.rotZ = 0.0F;
		}
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
