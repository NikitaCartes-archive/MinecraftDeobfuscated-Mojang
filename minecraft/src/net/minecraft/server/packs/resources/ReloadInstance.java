package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import net.minecraft.util.Unit;

public interface ReloadInstance {
	CompletableFuture<Unit> done();

	float getActualProgress();

	boolean isDone();

	void checkExceptions();
}
