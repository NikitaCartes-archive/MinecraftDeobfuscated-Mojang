package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class EntityCatSplitFix extends SimpleEntityRenameFix {
	public EntityCatSplitFix(Schema schema, boolean bl) {
		super("EntityCatSplitFix", schema, bl);
	}

	@Override
	protected Pair<String, Dynamic<?>> getNewNameAndTag(String string, Dynamic<?> dynamic) {
		if (Objects.equals("minecraft:ocelot", string)) {
			int i = dynamic.get("CatType").asInt(0);
			if (i == 0) {
				String string2 = dynamic.get("Owner").asString("");
				String string3 = dynamic.get("OwnerUUID").asString("");
				if (string2.length() > 0 || string3.length() > 0) {
					dynamic.set("Trusting", dynamic.createBoolean(true));
				}
			} else if (i > 0 && i < 4) {
				dynamic = dynamic.set("CatType", dynamic.createInt(i));
				dynamic = dynamic.set("OwnerUUID", dynamic.createString(dynamic.get("OwnerUUID").asString("")));
				return Pair.of("minecraft:cat", dynamic);
			}
		}

		return Pair.of(string, dynamic);
	}
}
