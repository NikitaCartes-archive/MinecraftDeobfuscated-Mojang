package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Objects;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.SerializableLong;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;

public class WorkAtPoi extends Behavior<Villager> {
	private long lastCheck;

	public WorkAtPoi() {
		super(ImmutableMap.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
		if (serverLevel.getGameTime() - this.lastCheck < 300L) {
			return false;
		} else if (serverLevel.random.nextInt(2) != 0) {
			return false;
		} else {
			this.lastCheck = serverLevel.getGameTime();
			GlobalPos globalPos = (GlobalPos)villager.getBrain().getMemory(MemoryModuleType.JOB_SITE).get();
			return Objects.equals(globalPos.dimension(), serverLevel.getDimension().getType()) && globalPos.pos().closerThan(villager.position(), 1.73);
		}
	}

	protected void start(ServerLevel serverLevel, Villager villager, long l) {
		Brain<Villager> brain = villager.getBrain();
		brain.setMemory(MemoryModuleType.LAST_WORKED_AT_POI, SerializableLong.of(l));
		brain.getMemory(MemoryModuleType.JOB_SITE).ifPresent(globalPos -> brain.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosWrapper(globalPos.pos())));
		villager.playWorkSound();
		if (villager.shouldRestock()) {
			villager.restock();
		}
	}
}
