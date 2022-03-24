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
    public static final Activity FIGHT = Activity.register("fight");
    public static final Activity CELEBRATE = Activity.register("celebrate");
    public static final Activity ADMIRE_ITEM = Activity.register("admire_item");
    public static final Activity AVOID = Activity.register("avoid");
    public static final Activity RIDE = Activity.register("ride");
    public static final Activity PLAY_DEAD = Activity.register("play_dead");
    public static final Activity LONG_JUMP = Activity.register("long_jump");
    public static final Activity RAM = Activity.register("ram");
    public static final Activity TONGUE = Activity.register("tongue");
    public static final Activity SWIM = Activity.register("swim");
    public static final Activity LAY_SPAWN = Activity.register("lay_spawn");
    public static final Activity SNIFF = Activity.register("sniff");
    public static final Activity INVESTIGATE = Activity.register("investigate");
    public static final Activity ROAR = Activity.register("roar");
    public static final Activity EMERGE = Activity.register("emerge");
    public static final Activity DIG = Activity.register("dig");
    private final String name;
    private final int hashCode;

    private Activity(String string) {
        this.name = string;
        this.hashCode = string.hashCode();
    }

    public String getName() {
        return this.name;
    }

    private static Activity register(String string) {
        return Registry.register(Registry.ACTIVITY, string, new Activity(string));
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        Activity activity = (Activity)object;
        return this.name.equals(activity.name);
    }

    public int hashCode() {
        return this.hashCode;
    }

    public String toString() {
        return this.getName();
    }
}

