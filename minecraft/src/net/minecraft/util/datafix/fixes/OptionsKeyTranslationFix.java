package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.stream.Collectors;

public class OptionsKeyTranslationFix extends DataFix {
	public OptionsKeyTranslationFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	public TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			"OptionsKeyTranslationFix",
			this.getInputSchema().getType(References.OPTIONS),
			typed -> typed.update(
					DSL.remainderFinder(),
					dynamic -> (Dynamic)dynamic.getMapValues()
							.map(map -> dynamic.createMap((Map<? extends Dynamic<?>, ? extends Dynamic<?>>)map.entrySet().stream().map(entry -> {
									if (((Dynamic)entry.getKey()).asString("").startsWith("key_")) {
										String string = ((Dynamic)entry.getValue()).asString("");
										if (!string.startsWith("key.mouse") && !string.startsWith("scancode.")) {
											return Pair.of(entry.getKey(), dynamic.createString("key.keyboard." + string.substring("key.".length())));
										}
									}

									return Pair.of(entry.getKey(), entry.getValue());
								}).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))))
							.orElse(dynamic)
				)
		);
	}
}
