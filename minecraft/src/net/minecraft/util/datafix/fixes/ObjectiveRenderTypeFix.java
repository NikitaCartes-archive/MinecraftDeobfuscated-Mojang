package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Optional;

public class ObjectiveRenderTypeFix extends DataFix {
	public ObjectiveRenderTypeFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	private static String getRenderType(String string) {
		return string.equals("health") ? "hearts" : "integer";
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.OBJECTIVE);
		return this.fixTypeEverywhereTyped("ObjectiveRenderTypeFix", type, typed -> typed.update(DSL.remainderFinder(), dynamic -> {
				Optional<String> optional = dynamic.get("RenderType").asString().result();
				if (optional.isEmpty()) {
					String string = dynamic.get("CriteriaName").asString("");
					String string2 = getRenderType(string);
					return dynamic.set("RenderType", dynamic.createString(string2));
				} else {
					return dynamic;
				}
			}));
	}
}
