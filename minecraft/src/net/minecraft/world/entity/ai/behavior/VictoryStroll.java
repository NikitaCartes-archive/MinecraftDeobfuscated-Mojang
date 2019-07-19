package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.raid.Raid;

public class VictoryStroll extends VillageBoundRandomStroll {
	public VictoryStroll(float f) {
		super(f);
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
		Raid raid = serverLevel.getRaidAt(new BlockPos(pathfinderMob));
		return raid != null && raid.isVictory() && super.checkExtraStartConditions(serverLevel, pathfinderMob);
	}
}
