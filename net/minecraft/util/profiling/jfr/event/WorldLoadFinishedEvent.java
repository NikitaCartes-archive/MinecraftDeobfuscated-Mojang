/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.jfr.event;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;
import net.minecraft.obfuscate.DontObfuscate;

@Name(value="minecraft.LoadWorld")
@Label(value="Create/Load World")
@Category(value={"Minecraft", "World Generation"})
@StackTrace(value=false)
@DontObfuscate
public class WorldLoadFinishedEvent
extends Event {
    public static final String EVENT_NAME = "minecraft.LoadWorld";
}

