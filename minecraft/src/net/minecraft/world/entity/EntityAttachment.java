package net.minecraft.world.entity;

import java.util.List;
import net.minecraft.world.phys.Vec3;

public enum EntityAttachment {
	PASSENGER(EntityAttachment.Fallback.AT_HEIGHT),
	VEHICLE(EntityAttachment.Fallback.AT_FEET),
	NAME_TAG(EntityAttachment.Fallback.AT_HEIGHT);

	private final EntityAttachment.Fallback fallback;

	private EntityAttachment(EntityAttachment.Fallback fallback) {
		this.fallback = fallback;
	}

	public List<Vec3> createFallbackPoints(float f, float g) {
		return this.fallback.create(f, g);
	}

	public interface Fallback {
		List<Vec3> ZERO = List.of(Vec3.ZERO);
		EntityAttachment.Fallback AT_FEET = (f, g) -> ZERO;
		EntityAttachment.Fallback AT_HEIGHT = (f, g) -> List.of(new Vec3(0.0, (double)g, 0.0));

		List<Vec3> create(float f, float g);
	}
}
