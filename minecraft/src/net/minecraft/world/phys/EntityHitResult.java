package net.minecraft.world.phys;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

public class EntityHitResult extends HitResult {
	private final Entity entity;
	private final float interactionDistance;

	public EntityHitResult(Entity entity, float f) {
		this(entity, entity.position(), f);
	}

	public EntityHitResult(Entity entity, Vec3 vec3, float f) {
		super(vec3);
		this.entity = entity;
		this.interactionDistance = f;
	}

	public Entity getEntity() {
		return this.entity;
	}

	@Override
	public HitResult.Type getType() {
		return HitResult.Type.ENTITY;
	}

	@Environment(EnvType.CLIENT)
	public float getInteractionDistance() {
		return this.interactionDistance;
	}
}
