package net.minecraft.world.damagesource;

import net.minecraft.world.phys.Vec3;

public class PointDamageSource extends DamageSource {
	private final Vec3 damageSourcePosition;

	public PointDamageSource(String string, Vec3 vec3) {
		super(string);
		this.damageSourcePosition = vec3;
	}

	@Override
	public Vec3 getSourcePosition() {
		return this.damageSourcePosition;
	}
}
