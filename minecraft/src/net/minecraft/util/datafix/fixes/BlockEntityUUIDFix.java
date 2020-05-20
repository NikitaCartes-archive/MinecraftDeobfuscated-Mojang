package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class BlockEntityUUIDFix extends AbstractUUIDFix {
	public BlockEntityUUIDFix(Schema schema) {
		super(schema, References.BLOCK_ENTITY);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped("BlockEntityUUIDFix", this.getInputSchema().getType(this.typeReference), typed -> {
			typed = this.updateNamedChoice(typed, "minecraft:conduit", this::updateConduit);
			return this.updateNamedChoice(typed, "minecraft:skull", this::updateSkull);
		});
	}

	private Dynamic<?> updateSkull(Dynamic<?> dynamic) {
		return (Dynamic<?>)dynamic.get("Owner")
			.get()
			.map(dynamicx -> (Dynamic)replaceUUIDString(dynamicx, "Id", "Id").orElse(dynamicx))
			.map(dynamic2 -> dynamic.remove("Owner").set("SkullOwner", dynamic2))
			.result()
			.orElse(dynamic);
	}

	private Dynamic<?> updateConduit(Dynamic<?> dynamic) {
		return (Dynamic<?>)replaceUUIDMLTag(dynamic, "target_uuid", "Target").orElse(dynamic);
	}
}
