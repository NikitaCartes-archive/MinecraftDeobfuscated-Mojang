package net.minecraft.util.profiling.jfr.event;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;
import jdk.jfr.Timespan;
import net.minecraft.obfuscate.DontObfuscate;

@Name("minecraft.ServerTickTime")
@Label("Server Tick Time")
@Category({"Minecraft", "Ticking"})
@StackTrace(false)
@DontObfuscate
public class ServerTickTimeEvent extends Event {
	public static final String EVENT_NAME = "minecraft.ServerTickTime";
	@Name("averageTickDuration")
	@Label("Average Server Tick Duration")
	@Timespan
	public final long averageTickDurationNanos;

	public ServerTickTimeEvent(float f) {
		this.averageTickDurationNanos = (long)(1000000.0F * f);
	}

	public static class Fields {
		public static final String AVERAGE_TICK_DURATION = "averageTickDuration";

		private Fields() {
		}
	}
}
