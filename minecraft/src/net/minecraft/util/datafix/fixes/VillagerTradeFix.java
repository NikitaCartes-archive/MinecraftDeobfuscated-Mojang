package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class VillagerTradeFix extends NamedEntityFix {
	public VillagerTradeFix(Schema schema, boolean bl) {
		super(schema, bl, "Villager trade fix", References.ENTITY, "minecraft:villager");
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		OpticFinder<?> opticFinder = typed.getType().findField("Offers");
		OpticFinder<?> opticFinder2 = opticFinder.type().findField("Recipes");
		if (!(opticFinder2.type() instanceof ListType<?> listType)) {
			throw new IllegalStateException("Recipes are expected to be a list.");
		} else {
			Type<?> type2 = listType.getElement();
			OpticFinder<?> opticFinder3 = DSL.typeFinder(type2);
			OpticFinder<?> opticFinder4 = type2.findField("buy");
			OpticFinder<?> opticFinder5 = type2.findField("buyB");
			OpticFinder<?> opticFinder6 = type2.findField("sell");
			OpticFinder<Pair<String, String>> opticFinder7 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
			Function<Typed<?>, Typed<?>> function = typedx -> this.updateItemStack(opticFinder7, typedx);
			return typed.updateTyped(
				opticFinder,
				typedx -> typedx.updateTyped(
						opticFinder2,
						typedxx -> typedxx.updateTyped(
								opticFinder3, typedxxx -> typedxxx.updateTyped(opticFinder4, function).updateTyped(opticFinder5, function).updateTyped(opticFinder6, function)
							)
					)
			);
		}
	}

	private Typed<?> updateItemStack(OpticFinder<Pair<String, String>> opticFinder, Typed<?> typed) {
		return typed.update(opticFinder, pair -> pair.mapSecond(string -> Objects.equals(string, "minecraft:carved_pumpkin") ? "minecraft:pumpkin" : string));
	}
}
