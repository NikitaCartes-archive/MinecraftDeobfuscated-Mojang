package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class ItemLoreFix extends DataFix {
	public ItemLoreFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
		OpticFinder<?> opticFinder = type.findField("tag");
		return this.fixTypeEverywhereTyped(
			"Item Lore componentize",
			type,
			typed -> typed.updateTyped(
					opticFinder,
					typedx -> typedx.update(
							DSL.remainderFinder(),
							dynamic -> dynamic.update(
									"display",
									dynamicx -> dynamicx.update(
											"Lore", dynamicxx -> DataFixUtils.orElse(dynamicxx.asStreamOpt().map(ItemLoreFix::fixLoreList).map(dynamicxx::createList).result(), dynamicxx)
										)
								)
						)
				)
		);
	}

	private static <T> Stream<Dynamic<T>> fixLoreList(Stream<Dynamic<T>> stream) {
		return stream.map(dynamic -> DataFixUtils.orElse(dynamic.asString().map(ItemLoreFix::fixLoreEntry).map(dynamic::createString).result(), dynamic));
	}

	private static String fixLoreEntry(String string) {
		return Component.Serializer.toJson(new TextComponent(string));
	}
}
