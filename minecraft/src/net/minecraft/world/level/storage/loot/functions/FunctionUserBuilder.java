package net.minecraft.world.level.storage.loot.functions;

import java.util.Arrays;
import java.util.function.Function;

public interface FunctionUserBuilder<T extends FunctionUserBuilder<T>> {
	T apply(LootItemFunction.Builder builder);

	default <E> T apply(Iterable<E> iterable, Function<E, LootItemFunction.Builder> function) {
		T functionUserBuilder = this.unwrap();

		for (E object : iterable) {
			functionUserBuilder = functionUserBuilder.apply((LootItemFunction.Builder)function.apply(object));
		}

		return functionUserBuilder;
	}

	default <E> T apply(E[] objects, Function<E, LootItemFunction.Builder> function) {
		return this.apply(Arrays.asList(objects), function);
	}

	T unwrap();
}
