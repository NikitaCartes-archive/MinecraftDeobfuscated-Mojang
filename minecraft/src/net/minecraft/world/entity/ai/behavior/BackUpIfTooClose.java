package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class BackUpIfTooClose<E extends Mob> extends Behavior<E> {
	private final int tooCloseDistance;
	private final float strafeSpeed;

	public BackUpIfTooClose(int i, float f) {
		super(
			ImmutableMap.of(
				MemoryModuleType.WALK_TARGET,
				MemoryStatus.VALUE_ABSENT,
				MemoryModuleType.LOOK_TARGET,
				MemoryStatus.REGISTERED,
				MemoryModuleType.ATTACK_TARGET,
				MemoryStatus.VALUE_PRESENT,
				MemoryModuleType.VISIBLE_LIVING_ENTITIES,
				MemoryStatus.VALUE_PRESENT
			)
		);
		this.tooCloseDistance = i;
		this.strafeSpeed = f;
	}

	protected boolean checkExtraStartConditions(ServerLevel serverLevel, E mob) {
		return this.isTargetVisible(mob) && this.isTargetTooClose(mob);
	}

	protected void start(ServerLevel serverLevel, E mob, long l) {
		mob.getMoveControl().strafe(-this.strafeSpeed, 0.0F);
		mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityPosWrapper(this.getTarget(mob)));
	}

	private boolean isTargetVisible(E mob) {
		return ((List)mob.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).get()).contains(this.getTarget(mob));
	}

	private boolean isTargetTooClose(E mob) {
		return this.getTarget(mob).closerThan(mob, (double)this.tooCloseDistance);
	}

	private LivingEntity getTarget(E mob) {
		return (LivingEntity)mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
	}
}
