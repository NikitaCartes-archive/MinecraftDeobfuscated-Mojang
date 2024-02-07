package net.minecraft.util.datafix;

import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.function.UnaryOperator;
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

	public static <T> Dynamic<T> setFieldIfPresent(Dynamic<T> dynamic, String string, Optional<? extends Dynamic<?>> optional) {
		return optional.isEmpty() ? dynamic : dynamic.set(string, (Dynamic<?>)optional.get());
	}

	public static <T> Dynamic<T> renameField(Dynamic<T> dynamic, String string, String string2) {
		return renameAndFixField(dynamic, string, string2, UnaryOperator.identity());
	}

	public static <T> Dynamic<T> replaceField(Dynamic<T> dynamic, String string, String string2, Optional<? extends Dynamic<?>> optional) {
		return setFieldIfPresent(dynamic.remove(string), string2, optional);
	}

	public static <T> Dynamic<T> renameAndFixField(Dynamic<T> dynamic, String string, String string2, UnaryOperator<Dynamic<?>> unaryOperator) {
		return setFieldIfPresent(dynamic.remove(string), string2, dynamic.get(string).result().map(unaryOperator));
	}
}
