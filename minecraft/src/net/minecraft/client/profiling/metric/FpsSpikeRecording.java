package net.minecraft.client.profiling.metric;

import java.util.Date;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.profiling.ProfileResults;

@Environment(EnvType.CLIENT)
public final class FpsSpikeRecording {
	public final Date timestamp;
	public final int tick;
	public final ProfileResults profilerResultForSpikeFrame;

	public FpsSpikeRecording(Date date, int i, ProfileResults profileResults) {
		this.timestamp = date;
		this.tick = i;
		this.profilerResultForSpikeFrame = profileResults;
	}
}
