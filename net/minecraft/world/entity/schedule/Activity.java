/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.schedule;

import net.minecraft.core.Registry;

public class Activity {
    public static final Activity CORE = Activity.register("core");
    public static final Activity IDLE = Activity.register("idle");
    public static final Activity WORK = Activity.register("work");
    public static final Activity PLAY = Activity.register("play");
    public static final Activity REST = Activity.register("rest");
    public static final Activity MEET = Activity.register("meet");
    public static final Activity PANIC = Activity.register("panic");
    public static final Activity RAID = Activity.register("raid");
    public static final Activity PRE_RAID = Activity.register("pre_raid");
    public static final Activity HIDE = Activity.register("hide");
    private final String name;

    private Activity(String string) {
        this.name = string;
    }

    public String getName() {
        return this.name;
    }

    private static Activity register(String string) {
        return Registry.register(Registry.ACTIVITY, string, new Activity(string));
    }

    public String toString() {
        return this.getName();
    }
}

