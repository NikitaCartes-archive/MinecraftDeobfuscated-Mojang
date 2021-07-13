package net.minecraft.util.profiling.jfr.stats;

import jdk.jfr.consumer.RecordedEvent;

public class CpuLoadStat {
	public final double jvm;
	public final double userJvm;
	public final double system;

	public CpuLoadStat(RecordedEvent recordedEvent) {
		this.jvm = (double)recordedEvent.getFloat("jvmSystem");
		this.userJvm = (double)recordedEvent.getFloat("jvmUser");
		this.system = (double)recordedEvent.getFloat("machineTotal");
	}
}
