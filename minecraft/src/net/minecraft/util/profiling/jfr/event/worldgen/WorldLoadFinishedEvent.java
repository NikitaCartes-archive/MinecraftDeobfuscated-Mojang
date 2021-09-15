package net.minecraft.util.profiling.jfr.event.worldgen;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;
import net.minecraft.obfuscate.DontObfuscate;

@Name("minecraft.WorldLoadFinishedEvent")
@Label("World create/load duration")
@Category({"Minecraft", "World Generation"})
@StackTrace(false)
@DontObfuscate
public class WorldLoadFinishedEvent extends Event {
	public static final String EVENT_NAME = "minecraft.WorldLoadFinishedEvent";
}
