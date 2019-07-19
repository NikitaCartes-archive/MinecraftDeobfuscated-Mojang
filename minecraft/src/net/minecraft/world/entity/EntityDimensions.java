package net.minecraft.world.entity;

public class EntityDimensions {
	public final float width;
	public final float height;
	public final boolean fixed;

	public EntityDimensions(float f, float g, boolean bl) {
		this.width = f;
		this.height = g;
		this.fixed = bl;
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
