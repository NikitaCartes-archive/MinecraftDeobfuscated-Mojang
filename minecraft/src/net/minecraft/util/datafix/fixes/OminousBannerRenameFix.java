package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class OminousBannerRenameFix extends DataFix {
	public OminousBannerRenameFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	private Dynamic<?> fixTag(Dynamic<?> dynamic) {
		Optional<? extends Dynamic<?>> optional = dynamic.get("display").result();
		if (optional.isPresent()) {
			Dynamic<?> dynamic2 = (Dynamic<?>)optional.get();
			Optional<String> optional2 = dynamic2.get("Name").asString().result();
			if (optional2.isPresent()) {
				String string = (String)optional2.get();
				string = string.replace("\"translate\":\"block.minecraft.illager_banner\"", "\"translate\":\"block.minecraft.ominous_banner\"");
				dynamic2 = dynamic2.set("Name", dynamic2.createString(string));
			}

			return dynamic.set("display", dynamic2);
		} else {
			return dynamic;
		}
	}

	@Override
	public TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
		OpticFinder<Pair<String, String>> opticFinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
		OpticFinder<?> opticFinder2 = type.findField("tag");
		return this.fixTypeEverywhereTyped("OminousBannerRenameFix", type, typed -> {
			Optional<Pair<String, String>> optional = typed.getOptional(opticFinder);
			if (optional.isPresent() && Objects.equals(((Pair)optional.get()).getSecond(), "minecraft:white_banner")) {
				Optional<? extends Typed<?>> optional2 = typed.getOptionalTyped(opticFinder2);
				if (optional2.isPresent()) {
					Typed<?> typed2 = (Typed<?>)optional2.get();
					Dynamic<?> dynamic = typed2.get(DSL.remainderFinder());
					return typed.set(opticFinder2, typed2.set(DSL.remainderFinder(), this.fixTag(dynamic)));
				}
			}

			return typed;
		});
	}
}
