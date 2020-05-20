package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class JigsawPropertiesFix extends NamedEntityFix {
	public JigsawPropertiesFix(Schema schema, boolean bl) {
		super(schema, bl, "JigsawPropertiesFix", References.BLOCK_ENTITY, "minecraft:jigsaw");
	}

	private static Dynamic<?> fixTag(Dynamic<?> dynamic) {
		String string = dynamic.get("attachement_type").asString("minecraft:empty");
		String string2 = dynamic.get("target_pool").asString("minecraft:empty");
		return dynamic.set("name", dynamic.createString(string))
			.set("target", dynamic.createString(string))
			.remove("attachement_type")
			.set("pool", dynamic.createString(string2))
			.remove("target_pool");
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), JigsawPropertiesFix::fixTag);
	}
}
