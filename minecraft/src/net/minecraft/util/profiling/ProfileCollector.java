package net.minecraft.util.profiling;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface ProfileCollector extends ProfilerFiller {
	@Override
	void push(String string);

	@Override
	void push(Supplier<String> supplier);

	@Override
	void pop();

	@Override
	void popPush(String string);

	@Environment(EnvType.CLIENT)
	@Override
	void popPush(Supplier<String> supplier);

	ProfileResults getResults();
}
