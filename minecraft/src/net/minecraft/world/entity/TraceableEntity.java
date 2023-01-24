package net.minecraft.world.entity;

import javax.annotation.Nullable;

public interface TraceableEntity {
	@Nullable
	Entity getOwner();
}
