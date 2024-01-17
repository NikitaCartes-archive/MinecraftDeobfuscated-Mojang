package net.minecraft.stats;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class Stat<T> extends ObjectiveCriteria {
	public static final StreamCodec<RegistryFriendlyByteBuf, Stat<?>> STREAM_CODEC = ByteBufCodecs.registry(Registries.STAT_TYPE)
		.dispatch(Stat::getType, StatType::streamCodec);
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
		return locationToKey(BuiltInRegistries.STAT_TYPE.getKey(statType)) + ":" + locationToKey(statType.getRegistry().getKey(object));
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
		return "Stat{name=" + this.getName() + ", formatter=" + this.formatter + "}";
	}
}
