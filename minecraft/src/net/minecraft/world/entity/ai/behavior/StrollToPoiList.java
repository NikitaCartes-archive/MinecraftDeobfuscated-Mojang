package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;

public class StrollToPoiList extends Behavior<Villager> {
	private final MemoryModuleType<List<GlobalPos>> strollToMemoryType;
	private final MemoryModuleType<GlobalPos> mustBeCloseToMemoryType;
	private final float speedModifier;
	private final int closeEnoughDist;
	private final int maxDistanceFromPoi;
	private long nextOkStartTime;
	@Nullable
	private GlobalPos targetPos;

	public StrollToPoiList(MemoryModuleType<List<GlobalPos>> memoryModuleType, float f, int i, int j, MemoryModuleType<GlobalPos> memoryModuleType2) {
		super(
			ImmutableMap.of(
				MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, memoryModuleType, MemoryStatus.VALUE_PRESENT, memoryModuleType2, MemoryStatus.VALUE_PRESENT
			)
		);
		this.strollToMemoryType = memoryModuleType;
		this.speedModifier = f;
		this.closeEnoughDist = i;
		this.maxDistanceFromPoi = j;
		this.mustBeCloseToMemoryType = memoryModuleType2;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
		Optional<List<GlobalPos>> optional = villager.getBrain().getMemory(this.strollToMemoryType);
		Optional<GlobalPos> optional2 = villager.getBrain().getMemory(this.mustBeCloseToMemoryType);
		if (optional.isPresent() && optional2.isPresent()) {
			List<GlobalPos> list = (List<GlobalPos>)optional.get();
			if (!list.isEmpty()) {
				this.targetPos = (GlobalPos)list.get(serverLevel.getRandom().nextInt(list.size()));
				return this.targetPos != null
					&& Objects.equals(serverLevel.getDimension().getType(), this.targetPos.dimension())
					&& ((GlobalPos)optional2.get()).pos().closerThan(villager.position(), (double)this.maxDistanceFromPoi);
			}
		}

		return false;
	}

	protected void start(ServerLevel serverLevel, Villager villager, long l) {
		if (l > this.nextOkStartTime && this.targetPos != null) {
			villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetPos.pos(), this.speedModifier, this.closeEnoughDist));
			this.nextOkStartTime = l + 100L;
		}
	}
}
