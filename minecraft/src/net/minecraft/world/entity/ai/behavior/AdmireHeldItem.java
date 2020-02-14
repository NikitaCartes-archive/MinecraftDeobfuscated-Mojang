package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;

public class AdmireHeldItem<E extends PathfinderMob> extends RunOne<E> {
	public AdmireHeldItem(float f) {
		super(ImmutableList.of(Pair.of(new RandomStroll(f, 1, 0), 1), Pair.of(new DoNothing(10, 20), 1)));
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E pathfinderMob) {
		return !pathfinderMob.getOffhandItem().isEmpty();
	}
}
