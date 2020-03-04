package net.minecraft.world.entity.ai.behavior;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;

public class EntityPosWrapper implements PositionWrapper {
	private final Entity entity;

	public EntityPosWrapper(Entity entity) {
		this.entity = entity;
	}

	@Override
	public BlockPos getPos() {
		return this.entity.blockPosition();
	}

	@Override
	public Vec3 getLookAtPos() {
		return new Vec3(this.entity.getX(), this.entity.getEyeY(), this.entity.getZ());
	}

	@Override
	public boolean isVisible(LivingEntity livingEntity) {
		Optional<List<LivingEntity>> optional = livingEntity.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES);
		return this.entity.isAlive() && optional.isPresent() && ((List)optional.get()).contains(this.entity);
	}

	public String toString() {
		return "EntityPosWrapper for " + this.entity;
	}
}
