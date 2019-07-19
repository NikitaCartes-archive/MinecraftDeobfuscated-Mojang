package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Unit;

public interface ReloadInstance {
	CompletableFuture<Unit> done();

	@Environment(EnvType.CLIENT)
	float getActualProgress();

	@Environment(EnvType.CLIENT)
	boolean isApplying();

	@Environment(EnvType.CLIENT)
	boolean isDone();

	@Environment(EnvType.CLIENT)
	void checkExceptions();
}
