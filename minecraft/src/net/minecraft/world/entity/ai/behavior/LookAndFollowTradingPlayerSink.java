package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

public class LookAndFollowTradingPlayerSink extends Behavior<Villager> {
	private final float speedModifier;

	public LookAndFollowTradingPlayerSink(float f) {
		super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED), Integer.MAX_VALUE);
		this.speedModifier = f;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
		Player player = villager.getTradingPlayer();
		return villager.isAlive()
			&& player != null
			&& !villager.isInWater()
			&& !villager.hurtMarked
			&& villager.distanceToSqr(player) <= 16.0
			&& player.containerMenu != null;
	}

	protected boolean canStillUse(ServerLevel serverLevel, Villager villager, long l) {
		return this.checkExtraStartConditions(serverLevel, villager);
	}

	protected void start(ServerLevel serverLevel, Villager villager, long l) {
		this.followPlayer(villager);
	}

	protected void stop(ServerLevel serverLevel, Villager villager, long l) {
		Brain<?> brain = villager.getBrain();
		brain.eraseMemory(MemoryModuleType.WALK_TARGET);
		brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
	}

	protected void tick(ServerLevel serverLevel, Villager villager, long l) {
		this.followPlayer(villager);
	}

	@Override
	protected boolean timedOut(long l) {
		return false;
	}

	private void followPlayer(Villager villager) {
		Brain<?> brain = villager.getBrain();
		brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(villager.getTradingPlayer(), false), this.speedModifier, 2));
		brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(villager.getTradingPlayer(), true));
	}
}
