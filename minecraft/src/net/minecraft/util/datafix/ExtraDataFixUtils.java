package net.minecraft.util.datafix;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.View;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.BitSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

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

	public static Type<?> patchSubType(Type<?> type, Type<?> type2, Type<?> type3) {
		return type.all(typePatcher(type2, type3), true, false).view().newType();
	}

	private static <A, B> TypeRewriteRule typePatcher(Type<A> type, Type<B> type2) {
		RewriteResult<A, B> rewriteResult = RewriteResult.create(View.create("Patcher", type, type2, dynamicOps -> object -> {
				throw new UnsupportedOperationException();
			}), new BitSet());
		return TypeRewriteRule.everywhere(TypeRewriteRule.ifSame(type, rewriteResult), PointFreeRule.nop(), true, true);
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

	public static Dynamic<?> blockState(String string, Map<String, String> map) {
		Dynamic<Tag> dynamic = new Dynamic<>(NbtOps.INSTANCE, new CompoundTag());
		Dynamic<Tag> dynamic2 = dynamic.set("Name", dynamic.createString(string));
		if (!map.isEmpty()) {
			dynamic2 = dynamic2.set(
				"Properties",
				dynamic.createMap(
					(Map<? extends Dynamic<?>, ? extends Dynamic<?>>)map.entrySet()
						.stream()
						.collect(Collectors.toMap(entry -> dynamic.createString((String)entry.getKey()), entry -> dynamic.createString((String)entry.getValue())))
				)
			);
		}

		return dynamic2;
	}

	public static Dynamic<?> blockState(String string) {
		return blockState(string, Map.of());
	}

	public static Dynamic<?> fixStringField(Dynamic<?> dynamic, String string, UnaryOperator<String> unaryOperator) {
		return dynamic.update(string, dynamic2 -> DataFixUtils.orElse(dynamic2.asString().map(unaryOperator).map(dynamic::createString).result(), dynamic2));
	}
}
