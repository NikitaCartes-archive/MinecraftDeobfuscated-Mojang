package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

public class SetWalkTargetFromBlockMemory extends Behavior<Villager> {
	private final MemoryModuleType<GlobalPos> memoryType;
	private final float speedModifier;
	private final int closeEnoughDist;
	private final int tooFarDistance;
	private final int tooLongUnreachableDuration;

	public SetWalkTargetFromBlockMemory(MemoryModuleType<GlobalPos> memoryModuleType, float f, int i, int j, int k) {
		super(
			ImmutableMap.of(
				MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
				MemoryStatus.REGISTERED,
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				memoryModuleType,
				MemoryStatus.VALUE_PRESENT
			)
		);
		this.memoryType = memoryModuleType;
		this.speedModifier = f;
		this.closeEnoughDist = i;
		this.tooFarDistance = j;
		this.tooLongUnreachableDuration = k;
	}

	private void dropPOI(Villager villager, long l) {
		Brain<?> brain = villager.getBrain();
		villager.releasePoi(this.memoryType);
		brain.eraseMemory(this.memoryType);
		brain.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, l);
	}

	protected void start(ServerLevel serverLevel, Villager villager, long l) {
		Brain<?> brain = villager.getBrain();
		brain.getMemory(this.memoryType).ifPresent(globalPos -> {
			if (this.wrongDimension(serverLevel, globalPos) || this.tiredOfTryingToFindTarget(serverLevel, villager)) {
				this.dropPOI(villager, l);
			} else if (this.tooFar(villager, globalPos)) {
				Vec3 vec3 = null;
				int i = 0;

				for (int j = 1000; i < 1000 && (vec3 == null || this.tooFar(villager, GlobalPos.of(serverLevel.dimension(), new BlockPos(vec3)))); i++) {
					vec3 = RandomPos.getPosTowards(villager, 15, 7, Vec3.atBottomCenterOf(globalPos.pos()));
				}

				if (i == 1000) {
					this.dropPOI(villager, l);
					return;
				}

				brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3, this.speedModifier, this.closeEnoughDist));
			} else if (!this.closeEnough(serverLevel, villager, globalPos)) {
				brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(globalPos.pos(), this.speedModifier, this.closeEnoughDist));
			}
		});
	}

	private boolean tiredOfTryingToFindTarget(ServerLevel serverLevel, Villager villager) {
		Optional<Long> optional = villager.getBrain().getMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
		return optional.isPresent() ? serverLevel.getGameTime() - (Long)optional.get() > (long)this.tooLongUnreachableDuration : false;
	}

	private boolean tooFar(Villager villager, GlobalPos globalPos) {
		return globalPos.pos().distManhattan(villager.blockPosition()) > this.tooFarDistance;
	}

	private boolean wrongDimension(ServerLevel serverLevel, GlobalPos globalPos) {
		return globalPos.dimension() != serverLevel.dimension();
	}

	private boolean closeEnough(ServerLevel serverLevel, Villager villager, GlobalPos globalPos) {
		return globalPos.dimension() == serverLevel.dimension() && globalPos.pos().distManhattan(villager.blockPosition()) <= this.closeEnoughDist;
	}
}
