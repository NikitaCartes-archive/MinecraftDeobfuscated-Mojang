package net.minecraft.util.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicLike;
import com.mojang.serialization.OptionalDynamic;
import java.util.Arrays;
import java.util.List;
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

	public static Dynamic<?> copyField(Dynamic<?> dynamic, String string, Dynamic<?> dynamic2, String string2) {
		return copyAndFixField(dynamic, string, dynamic2, string2, UnaryOperator.identity());
	}

	public static <T> Dynamic<?> copyAndFixField(Dynamic<T> dynamic, String string, Dynamic<?> dynamic2, String string2, UnaryOperator<Dynamic<T>> unaryOperator) {
		Optional<Dynamic<T>> optional = dynamic.get(string).result();
		return optional.isPresent() ? dynamic2.set(string2, (Dynamic<?>)unaryOperator.apply((Dynamic)optional.get())) : dynamic2;
	}

	@SafeVarargs
	public static TypeTemplate optionalFields(Pair<String, TypeTemplate>... pairs) {
		List<TypeTemplate> list = Arrays.stream(pairs).map(pair -> DSL.optional(DSL.field((String)pair.getFirst(), (TypeTemplate)pair.getSecond()))).toList();
		return DSL.allWithRemainder((TypeTemplate)list.get(0), (TypeTemplate[])list.subList(1, list.size()).toArray(new TypeTemplate[0]));
	}

	private static <T> DataResult<Boolean> asBoolean(Dynamic<T> dynamic) {
		return dynamic.getOps().getBooleanValue(dynamic.getValue());
	}

	public static DataResult<Boolean> asBoolean(DynamicLike<?> dynamicLike) {
		if (dynamicLike instanceof Dynamic<?> dynamic) {
			return asBoolean(dynamic);
		} else {
			return dynamicLike instanceof OptionalDynamic<?> optionalDynamic
				? optionalDynamic.get().flatMap(ExtraDataFixUtils::asBoolean)
				: DataResult.error(() -> "Unknown dynamic value: " + dynamicLike);
		}
	}

	public static boolean asBoolean(DynamicLike<?> dynamicLike, boolean bl) {
		return (Boolean)asBoolean(dynamicLike).result().orElse(bl);
	}

	public static <T, R> Typed<R> cast(Type<R> type, Typed<T> typed) {
		return new Typed<>(type, typed.getOps(), (R)typed.getValue());
	}
}
