package net.minecraft.world.entity;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record EntityDimensions(float width, float height, float eyeHeight, EntityAttachments attachments, boolean fixed) {
	private EntityDimensions(float f, float g, boolean bl) {
		this(f, g, defaultEyeHeight(g), EntityAttachments.createDefault(f, g), bl);
	}

	private static float defaultEyeHeight(float f) {
		return f * 0.85F;
	}

	public AABB makeBoundingBox(Vec3 vec3) {
		return this.makeBoundingBox(vec3.x, vec3.y, vec3.z);
	}

	public AABB makeBoundingBox(double d, double e, double f) {
		float g = this.width / 2.0F;
		float h = this.height;
		return new AABB(d - (double)g, e, f - (double)g, d + (double)g, e + (double)h, f + (double)g);
	}

	public EntityDimensions scale(float f) {
		return this.scale(f, f);
	}

	public EntityDimensions scale(float f, float g) {
		return !this.fixed && (f != 1.0F || g != 1.0F)
			? new EntityDimensions(this.width * f, this.height * g, this.eyeHeight * g, this.attachments.scale(f, g, f), false)
			: this;
	}

	public static EntityDimensions scalable(float f, float g) {
		return new EntityDimensions(f, g, false);
	}

	public static EntityDimensions fixed(float f, float g) {
		return new EntityDimensions(f, g, true);
	}

	public EntityDimensions withEyeHeight(float f) {
		return new EntityDimensions(this.width, this.height, f, this.attachments, this.fixed);
	}

	public EntityDimensions withAttachments(EntityAttachments.Builder builder) {
		return new EntityDimensions(this.width, this.height, this.eyeHeight, builder.build(this.width, this.height), this.fixed);
	}
}
