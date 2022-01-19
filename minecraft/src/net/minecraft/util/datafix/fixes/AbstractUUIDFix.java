package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public abstract class AbstractUUIDFix extends DataFix {
	protected TypeReference typeReference;

	public AbstractUUIDFix(Schema schema, TypeReference typeReference) {
		super(schema, false);
		this.typeReference = typeReference;
	}

	protected Typed<?> updateNamedChoice(Typed<?> typed, String string, Function<Dynamic<?>, Dynamic<?>> function) {
		Type<?> type = this.getInputSchema().getChoiceType(this.typeReference, string);
		Type<?> type2 = this.getOutputSchema().getChoiceType(this.typeReference, string);
		return typed.updateTyped(DSL.namedChoice(string, type), type2, typedx -> typedx.update(DSL.remainderFinder(), function));
	}

	protected static Optional<Dynamic<?>> replaceUUIDString(Dynamic<?> dynamic, String string, String string2) {
		return createUUIDFromString(dynamic, string).map(dynamic2 -> dynamic.remove(string).set(string2, dynamic2));
	}

	protected static Optional<Dynamic<?>> replaceUUIDMLTag(Dynamic<?> dynamic, String string, String string2) {
		return dynamic.get(string).result().flatMap(AbstractUUIDFix::createUUIDFromML).map(dynamic2 -> dynamic.remove(string).set(string2, dynamic2));
	}

	protected static Optional<Dynamic<?>> replaceUUIDLeastMost(Dynamic<?> dynamic, String string, String string2) {
		String string3 = string + "Most";
		String string4 = string + "Least";
		return createUUIDFromLongs(dynamic, string3, string4).map(dynamic2 -> dynamic.remove(string3).remove(string4).set(string2, dynamic2));
	}

	protected static Optional<Dynamic<?>> createUUIDFromString(Dynamic<?> dynamic, String string) {
		return dynamic.get(string).result().flatMap(dynamic2 -> {
			String stringx = dynamic2.asString(null);
			if (stringx != null) {
				try {
					UUID uUID = UUID.fromString(stringx);
					return createUUIDTag(dynamic, uUID.getMostSignificantBits(), uUID.getLeastSignificantBits());
				} catch (IllegalArgumentException var4) {
				}
			}

			return Optional.empty();
		});
	}

	protected static Optional<Dynamic<?>> createUUIDFromML(Dynamic<?> dynamic) {
		return createUUIDFromLongs(dynamic, "M", "L");
	}

	protected static Optional<Dynamic<?>> createUUIDFromLongs(Dynamic<?> dynamic, String string, String string2) {
		long l = dynamic.get(string).asLong(0L);
		long m = dynamic.get(string2).asLong(0L);
		return l != 0L && m != 0L ? createUUIDTag(dynamic, l, m) : Optional.empty();
	}

	protected static Optional<Dynamic<?>> createUUIDTag(Dynamic<?> dynamic, long l, long m) {
		return Optional.of(dynamic.createIntList(Arrays.stream(new int[]{(int)(l >> 32), (int)l, (int)(m >> 32), (int)m})));
	}
}
