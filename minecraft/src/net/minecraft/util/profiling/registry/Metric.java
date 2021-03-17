package net.minecraft.util.profiling.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Metric {
	private final String name;

	public Metric(String string) {
		this.name = string;
	}

	public String getName() {
		return this.name;
	}

	public String toString() {
		return "Metric{name='" + this.name + '\'' + '}';
	}
}
