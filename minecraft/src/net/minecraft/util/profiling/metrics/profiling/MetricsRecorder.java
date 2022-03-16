package net.minecraft.util.profiling.metrics.profiling;

import net.minecraft.util.profiling.ProfilerFiller;

public interface MetricsRecorder {
	void end();

	void cancel();

	void startTick();

	boolean isRecording();

	ProfilerFiller getProfiler();

	void endTick();
}
