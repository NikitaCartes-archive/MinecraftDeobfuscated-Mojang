package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class VillagerTradeFix extends DataFix {
	public VillagerTradeFix(Schema schema) {
		super(schema, false);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.VILLAGER_TRADE);
		OpticFinder<?> opticFinder = type.findField("buy");
		OpticFinder<?> opticFinder2 = type.findField("buyB");
		OpticFinder<?> opticFinder3 = type.findField("sell");
		OpticFinder<Pair<String, String>> opticFinder4 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
		Function<Typed<?>, Typed<?>> function = typed -> this.updateItemStack(opticFinder4, typed);
		return this.fixTypeEverywhereTyped(
			"Villager trade fix", type, typed -> typed.updateTyped(opticFinder, function).updateTyped(opticFinder2, function).updateTyped(opticFinder3, function)
		);
	}

	private Typed<?> updateItemStack(OpticFinder<Pair<String, String>> opticFinder, Typed<?> typed) {
		return typed.update(opticFinder, pair -> pair.mapSecond(string -> Objects.equals(string, "minecraft:carved_pumpkin") ? "minecraft:pumpkin" : string));
	}
}
