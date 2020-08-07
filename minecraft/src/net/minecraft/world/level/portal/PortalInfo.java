package net.minecraft.world.level.portal;

import net.minecraft.world.phys.Vec3;

public class PortalInfo {
	public final Vec3 pos;
	public final Vec3 speed;
	public final float yRot;
	public final float xRot;

	public PortalInfo(Vec3 vec3, Vec3 vec32, float f, float g) {
		this.pos = vec3;
		this.speed = vec32;
		this.yRot = f;
		this.xRot = g;
	}
}
