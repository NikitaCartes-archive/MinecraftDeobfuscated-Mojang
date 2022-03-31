package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.phys.Vec3;

public class FlyingRandomStroll extends RandomStroll {
	public FlyingRandomStroll(float f) {
		this(f, true);
	}

	public FlyingRandomStroll(float f, boolean bl) {
		super(f, bl);
	}

	@Override
	protected Vec3 getTargetPos(PathfinderMob pathfinderMob) {
		Vec3 vec3 = pathfinderMob.getViewVector(0.0F);
		return AirAndWaterRandomPos.getPos(pathfinderMob, this.maxHorizontalDistance, this.maxVerticalDistance, -2, vec3.x, vec3.z, (float) (Math.PI / 2));
	}
}
