package net.minecraft.world.level.gameevent;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface GameEventListener {
	PositionSource getListenerSource();

	int getListenerRadius();

	boolean handleGameEvent(Level level, GameEvent gameEvent, @Nullable Entity entity, Vec3 vec3);
}
