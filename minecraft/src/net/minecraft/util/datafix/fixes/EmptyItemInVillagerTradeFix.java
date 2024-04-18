package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EmptyItemInVillagerTradeFix extends DataFix {
	public EmptyItemInVillagerTradeFix(Schema schema) {
		super(schema, false);
	}

	@Override
	public TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.VILLAGER_TRADE);
		return this.writeFixAndRead("EmptyItemInVillagerTradeFix", type, type, dynamic -> {
			Dynamic<?> dynamic2 = dynamic.get("buyB").orElseEmptyMap();
			String string = NamespacedSchema.ensureNamespaced(dynamic2.get("id").asString("minecraft:air"));
			int i = dynamic2.get("count").asInt(0);
			return !string.equals("minecraft:air") && i != 0 ? dynamic : dynamic.remove("buyB");
		});
	}
}
