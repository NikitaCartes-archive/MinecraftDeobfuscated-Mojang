package net.minecraft.world.entity.schedule;

import net.minecraft.core.Registry;

public class Activity {
	public static final Activity CORE = register("core");
	public static final Activity IDLE = register("idle");
	public static final Activity WORK = register("work");
	public static final Activity PLAY = register("play");
	public static final Activity REST = register("rest");
	public static final Activity MEET = register("meet");
	public static final Activity PANIC = register("panic");
	public static final Activity RAID = register("raid");
	public static final Activity PRE_RAID = register("pre_raid");
	public static final Activity HIDE = register("hide");
	public static final Activity FIGHT = register("fight");
	public static final Activity CELEBRATE = register("celebrate");
	public static final Activity ADMIRE_ITEM = register("admire_item");
	public static final Activity AVOID = register("avoid");
	public static final Activity RIDE = register("ride");
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
