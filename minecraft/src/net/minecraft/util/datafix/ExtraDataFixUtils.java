package net.minecraft.util.datafix;

import com.mojang.datafixers.Typed;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

public class ExtraDataFixUtils {
	public static Dynamic<?> fixBlockPos(Dynamic<?> dynamic) {
		Optional<Number> optional = dynamic.get("X").asNumber().result();
		Optional<Number> optional2 = dynamic.get("Y").asNumber().result();
		Optional<Number> optional3 = dynamic.get("Z").asNumber().result();
		return !optional.isEmpty() && !optional2.isEmpty() && !optional3.isEmpty()
			? dynamic.createIntList(
				IntStream.of(new int[]{((Number)optional.get()).intValue(), ((Number)optional2.get()).intValue(), ((Number)optional3.get()).intValue()})
			)
			: dynamic;
	}

	public static <T, R> Typed<R> cast(Type<R> type, Typed<T> typed) {
		return new Typed<>(type, typed.getOps(), (R)typed.getValue());
	}

	@SafeVarargs
	public static <T> Function<Typed<?>, Typed<?>> chainAllFilters(Function<Typed<?>, Typed<?>>... functions) {
		return typed -> {
			for (Function<Typed<?>, Typed<?>> function : functions) {
				typed = (Typed)function.apply(typed);
			}

			return typed;
		};
	}
}
