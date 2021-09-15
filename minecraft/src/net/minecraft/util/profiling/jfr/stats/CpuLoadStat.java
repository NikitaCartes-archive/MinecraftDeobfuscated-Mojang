package net.minecraft.util.profiling.jfr.stats;

import jdk.jfr.consumer.RecordedEvent;

public record CpuLoadStat() {
	private final double jvm;
	private final double userJvm;
	private final double system;

	public CpuLoadStat(double d, double e, double f) {
		this.jvm = d;
		this.userJvm = e;
		this.system = f;
	}

	public static CpuLoadStat from(RecordedEvent recordedEvent) {
		return new CpuLoadStat((double)recordedEvent.getFloat("jvmSystem"), (double)recordedEvent.getFloat("jvmUser"), (double)recordedEvent.getFloat("machineTotal"));
	}
}
