package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.phys.Vec3;

public class WaterAvoidingRandomFlyingGoal extends WaterAvoidingRandomStrollGoal {
	public WaterAvoidingRandomFlyingGoal(PathfinderMob pathfinderMob, double d) {
		super(pathfinderMob, d);
	}

	@Nullable
	@Override
	protected Vec3 getPosition() {
		Vec3 vec3 = this.mob.getViewVector(0.0F);
		int i = 8;
		Vec3 vec32 = HoverRandomPos.getPos(this.mob, 8, 7, vec3.x, vec3.z, (float) (Math.PI / 2), 3, 1);
		return vec32 != null ? vec32 : AirAndWaterRandomPos.getPos(this.mob, 8, 4, -2, vec3.x, vec3.z, (float) (Math.PI / 2));
	}
}
