package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.Map;
import net.minecraft.util.datafix.ComponentDataFixUtils;

public class BannerEntityCustomNameToOverrideComponentFix extends DataFix {
	public BannerEntityCustomNameToOverrideComponentFix(Schema schema) {
		super(schema, false);
	}

	@Override
	public TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.BLOCK_ENTITY);
		TaggedChoiceType<?> taggedChoiceType = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
		OpticFinder<?> opticFinder = type.findField("components");
		return this.fixTypeEverywhereTyped("Banner entity custom_name to item_name component fix", type, typed -> {
			Object object = typed.get(taggedChoiceType.finder()).getFirst();
			return object.equals("minecraft:banner") ? this.fix(typed, opticFinder) : typed;
		});
	}

	private Typed<?> fix(Typed<?> typed, OpticFinder<?> opticFinder) {
		Dynamic<?> dynamic = (Dynamic<?>)typed.getOptional(DSL.remainderFinder()).orElseThrow();
		OptionalDynamic<?> optionalDynamic = dynamic.get("CustomName");
		boolean bl = optionalDynamic.asString()
			.result()
			.flatMap(ComponentDataFixUtils::extractTranslationString)
			.filter(string -> string.equals("block.minecraft.ominous_banner"))
			.isPresent();
		if (bl) {
			Typed<?> typed2 = typed.getOrCreateTyped(opticFinder)
				.update(
					DSL.remainderFinder(),
					dynamicx -> dynamicx.set("minecraft:item_name", (Dynamic<?>)optionalDynamic.result().get())
							.set("minecraft:hide_additional_tooltip", dynamicx.createMap(Map.of()))
				);
			return typed.set(opticFinder, typed2).set(DSL.remainderFinder(), dynamic.remove("CustomName"));
		} else {
			return typed;
		}
	}
}
