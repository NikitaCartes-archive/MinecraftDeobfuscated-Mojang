package net.minecraft.util.profiling.jfr.event.ticking;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.Period;
import jdk.jfr.StackTrace;
import jdk.jfr.Timespan;
import net.minecraft.obfuscate.DontObfuscate;

@Name("minecraft.ServerTickTime")
@Label("Server tick time")
@Category({"Minecraft", "Ticking"})
@StackTrace(false)
@Period("1s")
@DontObfuscate
public class ServerTickTimeEvent extends Event {
	public static final String EVENT_NAME = "minecraft.ServerTickTime";
	@Name("averageTickMs")
	@Label("Average server tick time (ms)")
	@Timespan("MILLISECONDS")
	public final float averageTickMs;

	public ServerTickTimeEvent(float f) {
		this.averageTickMs = f;
	}

	public static class Fields {
		public static final String AVERAGE_TICK_MS = "averageTickMs";

		private Fields() {
		}
	}
}
