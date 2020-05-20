package net.minecraft.world.level.block.state.properties;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.world.level.block.state.StateHolder;

public abstract class Property<T extends Comparable<T>> {
	private final Class<T> clazz;
	private final String name;
	private Integer hashCode;
	private final Codec<T> codec = Codec.STRING
		.comapFlatMap(
			stringx -> (DataResult)this.getValue(stringx)
					.map(DataResult::success)
					.orElseGet(() -> DataResult.error("Unable to read property: " + this + " with value: " + stringx)),
			this::getName
		);

	protected Property(String string, Class<T> class_) {
		this.clazz = class_;
		this.name = string;
	}

	public String getName() {
		return this.name;
	}

	public Class<T> getValueClass() {
		return this.clazz;
	}

	public abstract Collection<T> getPossibleValues();

	public abstract String getName(T comparable);

	public abstract Optional<T> getValue(String string);

	public String toString() {
		return MoreObjects.toStringHelper(this).add("name", this.name).add("clazz", this.clazz).add("values", this.getPossibleValues()).toString();
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof Property)) {
			return false;
		} else {
			Property<?> property = (Property<?>)object;
			return this.clazz.equals(property.clazz) && this.name.equals(property.name);
		}
	}

	public final int hashCode() {
		if (this.hashCode == null) {
			this.hashCode = this.generateHashCode();
		}

		return this.hashCode;
	}

	public int generateHashCode() {
		return 31 * this.clazz.hashCode() + this.name.hashCode();
	}

	public <U, S extends StateHolder<?, S>> DataResult<S> parseValue(DynamicOps<U> dynamicOps, S stateHolder, U object) {
		DataResult<T> dataResult = this.codec.parse(dynamicOps, object);
		return dataResult.map(comparable -> stateHolder.setValue(this, comparable)).setPartial(stateHolder);
	}
}
