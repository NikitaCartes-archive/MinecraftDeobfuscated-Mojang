package net.minecraft.world.phys;

public abstract class HitResult {
	protected final Vec3 location;

	protected HitResult(Vec3 vec3) {
		this.location = vec3;
	}

	public abstract HitResult.Type getType();

	public Vec3 getLocation() {
		return this.location;
	}

	public static enum Type {
		MISS,
		BLOCK,
		ENTITY;
	}
}
