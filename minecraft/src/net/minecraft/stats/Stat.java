package net.minecraft.stats;

import java.util.Objects;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class Stat<T> extends ObjectiveCriteria {
	private final StatFormatter formatter;
	private final T value;
	private final StatType<T> type;

	protected Stat(StatType<T> statType, T object, StatFormatter statFormatter) {
		super(buildName(statType, object));
		this.type = statType;
		this.formatter = statFormatter;
		this.value = object;
	}

	public static <T> String buildName(StatType<T> statType, T object) {
		return locationToKey(Registry.STAT_TYPE.getKey(statType)) + ":" + locationToKey(statType.getRegistry().getKey(object));
	}

	private static <T> String locationToKey(@Nullable ResourceLocation resourceLocation) {
		return resourceLocation.toString().replace(':', '.');
	}

	public StatType<T> getType() {
		return this.type;
	}

	public T getValue() {
		return this.value;
	}

	@Environment(EnvType.CLIENT)
	public String format(int i) {
		return this.formatter.format(i);
	}

	public boolean equals(Object object) {
		return this == object || object instanceof Stat && Objects.equals(this.getName(), ((Stat)object).getName());
	}

	public int hashCode() {
		return this.getName().hashCode();
	}

	public String toString() {
		return "Stat{name=" + this.getName() + ", formatter=" + this.formatter + '}';
	}
}
