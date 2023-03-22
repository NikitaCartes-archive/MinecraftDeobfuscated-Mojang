package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FeatureFlagRemoveFix extends DataFix {
	private final String name;
	private final Set<String> flagsToRemove;

	public FeatureFlagRemoveFix(Schema schema, String string, Set<String> set) {
		super(schema, false);
		this.name = string;
		this.flagsToRemove = set;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(References.LEVEL), typed -> typed.update(DSL.remainderFinder(), this::fixTag));
	}

	private <T> Dynamic<T> fixTag(Dynamic<T> dynamic) {
		List<Dynamic<T>> list = (List<Dynamic<T>>)dynamic.get("removed_features").asStream().collect(Collectors.toCollection(ArrayList::new));
		Dynamic<T> dynamic2 = dynamic.update(
			"enabled_features", dynamic2x -> DataFixUtils.orElse(dynamic2x.asStreamOpt().result().map(stream -> stream.filter(dynamic2xx -> {
						Optional<String> optional = dynamic2xx.asString().result();
						if (optional.isEmpty()) {
							return true;
						} else {
							boolean bl = this.flagsToRemove.contains(optional.get());
							if (bl) {
								list.add(dynamic.createString((String)optional.get()));
							}

							return !bl;
						}
					})).map(dynamic::createList), dynamic2x)
		);
		if (!list.isEmpty()) {
			dynamic2 = dynamic2.set("removed_features", dynamic.createList(list.stream()));
		}

		return dynamic2;
	}
}
