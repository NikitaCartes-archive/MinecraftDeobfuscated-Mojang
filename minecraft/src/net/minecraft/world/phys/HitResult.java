package net.minecraft.world.phys;

import net.minecraft.world.entity.Entity;

public abstract class HitResult {
	protected final Vec3 location;

	protected HitResult(Vec3 vec3) {
		this.location = vec3;
	}

	public double distanceTo(Entity entity) {
		double d = this.location.x - entity.getX();
		double e = this.location.y - entity.getY();
		double f = this.location.z - entity.getZ();
		return d * d + e * e + f * f;
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
