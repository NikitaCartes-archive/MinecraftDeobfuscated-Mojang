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

	void incrementCounter(String string);

	void incrementCounter(Supplier<String> supplier);

	static ProfilerFiller tee(ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2) {
		if (profilerFiller == InactiveProfiler.INSTANCE) {
			return profilerFiller2;
		} else {
			return profilerFiller2 == InactiveProfiler.INSTANCE ? profilerFiller : new ProfilerFiller() {
				@Override
				public void startTick() {
					profilerFiller.startTick();
					profilerFiller2.startTick();
				}

				@Override
				public void endTick() {
					profilerFiller.endTick();
					profilerFiller2.endTick();
				}

				@Override
				public void push(String string) {
					profilerFiller.push(string);
					profilerFiller2.push(string);
				}

				@Override
				public void push(Supplier<String> supplier) {
					profilerFiller.push(supplier);
					profilerFiller2.push(supplier);
				}

				@Override
				public void pop() {
					profilerFiller.pop();
					profilerFiller2.pop();
				}

				@Override
				public void popPush(String string) {
					profilerFiller.popPush(string);
					profilerFiller2.popPush(string);
				}

				@Environment(EnvType.CLIENT)
				@Override
				public void popPush(Supplier<String> supplier) {
					profilerFiller.popPush(supplier);
					profilerFiller2.popPush(supplier);
				}

				@Override
				public void incrementCounter(String string) {
					profilerFiller.incrementCounter(string);
					profilerFiller2.incrementCounter(string);
				}

				@Override
				public void incrementCounter(Supplier<String> supplier) {
					profilerFiller.incrementCounter(supplier);
					profilerFiller2.incrementCounter(supplier);
				}
			};
		}
	}
}
