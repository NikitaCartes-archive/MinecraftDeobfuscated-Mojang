package net.minecraft.util.profiling.jfr.serialize;

import java.io.IOException;
import net.minecraft.util.profiling.jfr.parse.JfrStatsResult;

public interface JfrResultSerializer {
	String format(JfrStatsResult jfrStatsResult) throws IOException;
}
