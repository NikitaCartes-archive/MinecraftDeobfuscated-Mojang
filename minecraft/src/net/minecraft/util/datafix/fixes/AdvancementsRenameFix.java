package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;

public class AdvancementsRenameFix extends DataFix {
	private final String name;
	private final Function<String, String> renamer;

	public AdvancementsRenameFix(Schema schema, boolean bl, String string, Function<String, String> function) {
		super(schema, bl);
		this.name = string;
		this.renamer = function;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			this.name, this.getInputSchema().getType(References.ADVANCEMENTS), typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.updateMapValues(pair -> {
						String string = ((Dynamic)pair.getFirst()).asString("");
						return pair.mapFirst(dynamic2 -> dynamic.createString((String)this.renamer.apply(string)));
					}))
		);
	}
}
