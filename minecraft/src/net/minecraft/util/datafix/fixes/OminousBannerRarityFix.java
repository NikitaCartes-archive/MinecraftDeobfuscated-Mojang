package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.datafix.ComponentDataFixUtils;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class OminousBannerRarityFix extends DataFix {
	public OminousBannerRarityFix(Schema schema) {
		super(schema, false);
	}

	@Override
	public TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.BLOCK_ENTITY);
		Type<?> type2 = this.getInputSchema().getType(References.ITEM_STACK);
		TaggedChoiceType<?> taggedChoiceType = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
		OpticFinder<Pair<String, String>> opticFinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
		OpticFinder<?> opticFinder2 = type.findField("components");
		OpticFinder<?> opticFinder3 = type2.findField("components");
		return TypeRewriteRule.seq(this.fixTypeEverywhereTyped("Ominous Banner block entity common rarity to uncommon rarity fix", type, typed -> {
			Object object = typed.get(taggedChoiceType.finder()).getFirst();
			return object.equals("minecraft:banner") ? this.fix(typed, opticFinder2) : typed;
		}), this.fixTypeEverywhereTyped("Ominous Banner item stack common rarity to uncommon rarity fix", type2, typed -> {
			String string = (String)typed.getOptional(opticFinder).map(Pair::getSecond).orElse("");
			return string.equals("minecraft:white_banner") ? this.fix(typed, opticFinder3) : typed;
		}));
	}

	private Typed<?> fix(Typed<?> typed, OpticFinder<?> opticFinder) {
		return typed.updateTyped(
			opticFinder,
			typedx -> typedx.update(
					DSL.remainderFinder(),
					dynamic -> {
						boolean bl = dynamic.get("minecraft:item_name")
							.asString()
							.result()
							.flatMap(ComponentDataFixUtils::extractTranslationString)
							.filter(string -> string.equals("block.minecraft.ominous_banner"))
							.isPresent();
						return bl
							? dynamic.set("minecraft:rarity", dynamic.createString("uncommon"))
								.set("minecraft:item_name", ComponentDataFixUtils.createTranslatableComponent(dynamic.getOps(), "block.minecraft.ominous_banner"))
							: dynamic;
					}
				)
		);
	}
}
