package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;

public class GoToPotentialJobSite extends Behavior<Villager> {
	final float speedModifier;

	public GoToPotentialJobSite(float f) {
		super(ImmutableMap.of(MemoryModuleType.POTENTIAL_JOB_SITE, MemoryStatus.VALUE_PRESENT), 1200);
		this.speedModifier = f;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
		return (Boolean)villager.getBrain()
			.getActiveNonCoreActivity()
			.map(activity -> activity == Activity.IDLE || activity == Activity.WORK || activity == Activity.PLAY)
			.orElse(true);
	}

	protected boolean canStillUse(ServerLevel serverLevel, Villager villager, long l) {
		return villager.getBrain().hasMemoryValue(MemoryModuleType.POTENTIAL_JOB_SITE);
	}

	protected void tick(ServerLevel serverLevel, Villager villager, long l) {
		BehaviorUtils.setWalkAndLookTargetMemories(
			villager, ((GlobalPos)villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get()).pos(), this.speedModifier, 1
		);
	}

	protected void stop(ServerLevel serverLevel, Villager villager, long l) {
		Optional<GlobalPos> optional = villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
		optional.ifPresent(globalPos -> {
			BlockPos blockPos = globalPos.pos();
			PoiManager poiManager = serverLevel.getServer().getLevel(globalPos.dimension()).getPoiManager();
			if (poiManager.exists(blockPos, poiType -> true)) {
				poiManager.release(blockPos);
			}

			DebugPackets.sendPoiTicketCountPacket(serverLevel, blockPos);
		});
		villager.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
	}
}
