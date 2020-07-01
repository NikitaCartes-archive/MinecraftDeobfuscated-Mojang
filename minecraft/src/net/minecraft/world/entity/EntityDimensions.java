package net.minecraft.world.entity;

import net.minecraft.world.phys.AABB;

public class EntityDimensions {
	public final float width;
	public final float height;
	public final boolean fixed;

	public EntityDimensions(float f, float g, boolean bl) {
		this.width = f;
		this.height = g;
		this.fixed = bl;
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
		return !this.fixed && (f != 1.0F || g != 1.0F) ? scalable(this.width * f, this.height * g) : this;
	}

	public static EntityDimensions scalable(float f, float g) {
		return new EntityDimensions(f, g, false);
	}

	public static EntityDimensions fixed(float f, float g) {
		return new EntityDimensions(f, g, true);
	}

	public String toString() {
		return "EntityDimensions w=" + this.width + ", h=" + this.height + ", fixed=" + this.fixed;
	}
}
