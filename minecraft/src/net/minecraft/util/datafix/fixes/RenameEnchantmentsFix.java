package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.function.Function;

public class RenameEnchantmentsFix extends DataFix {
	final String name;
	final Map<String, String> renames;

	public RenameEnchantmentsFix(Schema schema, String string, Map<String, String> map) {
		super(schema, false);
		this.name = string;
		this.renames = map;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
		OpticFinder<?> opticFinder = type.findField("tag");
		return this.fixTypeEverywhereTyped(this.name, type, typed -> typed.updateTyped(opticFinder, typedx -> typedx.update(DSL.remainderFinder(), this::fixTag)));
	}

	private Dynamic<?> fixTag(Dynamic<?> dynamic) {
		dynamic = this.fixEnchantmentList(dynamic, "Enchantments");
		return this.fixEnchantmentList(dynamic, "StoredEnchantments");
	}

	private Dynamic<?> fixEnchantmentList(Dynamic<?> dynamic, String string) {
		return dynamic.update(
			string,
			dynamicx -> dynamicx.asStreamOpt()
					.map(
						stream -> stream.map(
								dynamicxx -> dynamicxx.update(
										"id",
										dynamic2 -> dynamic2.asString()
												.map(stringx -> dynamicxx.createString((String)this.renames.getOrDefault(stringx, stringx)))
												.mapOrElse(Function.identity(), error -> dynamic2)
									)
							)
					)
					.map(dynamicx::createList)
					.mapOrElse(Function.identity(), error -> dynamicx)
		);
	}
}
