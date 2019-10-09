package net.minecraft.gametest.framework;

import javax.annotation.Nullable;

class GameTestEvent {
	@Nullable
	public final Long expectedDelay;
	public final Runnable assertion;
}
