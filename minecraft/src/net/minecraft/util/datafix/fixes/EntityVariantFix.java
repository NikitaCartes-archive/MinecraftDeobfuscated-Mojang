package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.function.Function;
import java.util.function.IntFunction;

public class EntityVariantFix extends NamedEntityFix {
	private final String fieldName;
	private final IntFunction<String> idConversions;

	public EntityVariantFix(Schema schema, String string, TypeReference typeReference, String string2, String string3, IntFunction<String> intFunction) {
		super(schema, false, string, typeReference, string2);
		this.fieldName = string3;
		this.idConversions = intFunction;
	}

	private static <T> Dynamic<T> updateAndRename(Dynamic<T> dynamic, String string, String string2, Function<Dynamic<T>, Dynamic<T>> function) {
		return dynamic.map(object -> {
			DynamicOps<T> dynamicOps = dynamic.getOps();
			Function<T, T> function2 = objectx -> ((Dynamic)function.apply(new Dynamic<>(dynamicOps, (T)objectx))).getValue();
			return dynamicOps.get((T)object, string).map(object2 -> dynamicOps.set((T)object, string2, (T)function2.apply(object2))).result().orElse(object);
		});
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(
			DSL.remainderFinder(),
			dynamic -> updateAndRename(
					dynamic,
					this.fieldName,
					"variant",
					dynamicx -> DataFixUtils.orElse(
							dynamicx.asNumber().map(number -> dynamicx.createString((String)this.idConversions.apply(number.intValue()))).result(), dynamicx
						)
				)
		);
	}
}
