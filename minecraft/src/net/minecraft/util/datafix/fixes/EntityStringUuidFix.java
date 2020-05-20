package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.Optional;
import java.util.UUID;

public class EntityStringUuidFix extends DataFix {
	public EntityStringUuidFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	public TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			"EntityStringUuidFix",
			this.getInputSchema().getType(References.ENTITY),
			typed -> typed.update(
					DSL.remainderFinder(),
					dynamic -> {
						Optional<String> optional = dynamic.get("UUID").asString().result();
						if (optional.isPresent()) {
							UUID uUID = UUID.fromString((String)optional.get());
							return dynamic.remove("UUID")
								.set("UUIDMost", dynamic.createLong(uUID.getMostSignificantBits()))
								.set("UUIDLeast", dynamic.createLong(uUID.getLeastSignificantBits()));
						} else {
							return dynamic;
						}
					}
				)
		);
	}
}
