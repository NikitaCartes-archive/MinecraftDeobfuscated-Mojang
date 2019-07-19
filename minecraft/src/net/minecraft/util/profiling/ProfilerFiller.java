package net.minecraft.util.profiling;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface ProfilerFiller {
	void startTick();

	void endTick();

	void push(String string);

	void push(Supplier<String> supplier);

	void pop();

	void popPush(String string);

	@Environment(EnvType.CLIENT)
	void popPush(Supplier<String> supplier);
}
