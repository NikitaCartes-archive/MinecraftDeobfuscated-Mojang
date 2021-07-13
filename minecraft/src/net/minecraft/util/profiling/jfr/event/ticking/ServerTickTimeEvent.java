package net.minecraft.util.profiling.jfr.event.ticking;

import jdk.jfr.Category;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.Period;
import jdk.jfr.Timespan;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.util.profiling.jfr.event.AbstractMinecraftJfrEvent;

@Name("minecraft.ServerTickTime")
@Label("Server tick time")
@Category({"Minecraft", "Ticking"})
@Period("1s")
@DontObfuscate
public class ServerTickTimeEvent extends AbstractMinecraftJfrEvent {
	public static final String NAME = "minecraft.ServerTickTime";
	public static final ServerTickTimeEvent EVENT = new ServerTickTimeEvent();
	@Label("Average server tick time (ms)")
	@Timespan("MILLISECONDS")
	public float averageTickMs;

	private ServerTickTimeEvent() {
	}

	public void reset() {
		this.averageTickMs = 0.0F;
	}
}
