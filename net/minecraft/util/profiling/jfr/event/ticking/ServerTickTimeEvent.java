/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.jfr.event.ticking;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.Period;
import jdk.jfr.StackTrace;
import jdk.jfr.Timespan;
import net.minecraft.obfuscate.DontObfuscate;

@Name(value="minecraft.ServerTickTime")
@Label(value="Server tick time")
@Category(value={"Minecraft", "Ticking"})
@StackTrace(value=false)
@Period(value="1s")
@DontObfuscate
public class ServerTickTimeEvent
extends Event {
    public static final String EVENT_NAME = "minecraft.ServerTickTime";
    @Name(value="averageTickMs")
    @Label(value="Average server tick time (ms)")
    @Timespan(value="MILLISECONDS")
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

