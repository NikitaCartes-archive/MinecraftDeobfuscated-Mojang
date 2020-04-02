package net.minecraft.world.entity.ai.behavior;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;

public class EntityTracker implements PositionTracker {
	private final Entity entity;

	public EntityTracker(Entity entity) {
		this.entity = entity;
	}

	@Override
	public Vec3 currentPosition() {
		return this.entity.position();
	}

	@Override
	public BlockPos currentBlockPosition() {
		return this.entity.blockPosition();
	}

	@Override
	public boolean isVisibleBy(LivingEntity livingEntity) {
		if (!(this.entity instanceof LivingEntity)) {
			return true;
		} else {
			Optional<List<LivingEntity>> optional = livingEntity.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES);
			return this.entity.isAlive() && optional.isPresent() && ((List)optional.get()).contains(this.entity);
		}
	}

	public String toString() {
		return "EntityTracker for " + this.entity;
	}
}
