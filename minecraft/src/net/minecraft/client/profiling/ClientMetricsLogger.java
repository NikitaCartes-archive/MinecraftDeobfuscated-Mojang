package net.minecraft.client.profiling;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.profiling.ProfilerFiller;

@Environment(EnvType.CLIENT)
public interface ClientMetricsLogger {
	void end();

	void startTick();

	boolean isRecording();

	ProfilerFiller getProfiler();

	void endTick();
}
