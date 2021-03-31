package net.minecraft.util.profiling.registry;

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
