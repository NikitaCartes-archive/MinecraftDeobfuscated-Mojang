package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.ComponentDataFixUtils;

public class ItemCustomNameToComponentFix extends DataFix {
	public ItemCustomNameToComponentFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	private Dynamic<?> fixTag(Dynamic<?> dynamic) {
		Optional<? extends Dynamic<?>> optional = dynamic.get("display").result();
		if (optional.isPresent()) {
			Dynamic<?> dynamic2 = (Dynamic<?>)optional.get();
			Optional<String> optional2 = dynamic2.get("Name").asString().result();
			if (optional2.isPresent()) {
				dynamic2 = dynamic2.set("Name", ComponentDataFixUtils.createPlainTextComponent(dynamic2.getOps(), (String)optional2.get()));
			} else {
				Optional<String> optional3 = dynamic2.get("LocName").asString().result();
				if (optional3.isPresent()) {
					dynamic2 = dynamic2.set("Name", ComponentDataFixUtils.createTranslatableComponent(dynamic2.getOps(), (String)optional3.get()));
					dynamic2 = dynamic2.remove("LocName");
				}
			}

			return dynamic.set("display", dynamic2);
		} else {
			return dynamic;
		}
	}

	@Override
	public TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
		OpticFinder<?> opticFinder = type.findField("tag");
		return this.fixTypeEverywhereTyped(
			"ItemCustomNameToComponentFix", type, typed -> typed.updateTyped(opticFinder, typedx -> typedx.update(DSL.remainderFinder(), this::fixTag))
		);
	}
}
