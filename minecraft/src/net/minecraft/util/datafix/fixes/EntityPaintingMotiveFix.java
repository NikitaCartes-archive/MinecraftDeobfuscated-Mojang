package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntityPaintingMotiveFix extends NamedEntityFix {
	private static final Map<String, String> MAP = DataFixUtils.make(Maps.<String, String>newHashMap(), hashMap -> {
		hashMap.put("donkeykong", "donkey_kong");
		hashMap.put("burningskull", "burning_skull");
		hashMap.put("skullandroses", "skull_and_roses");
	});

	public EntityPaintingMotiveFix(Schema schema, boolean bl) {
		super(schema, bl, "EntityPaintingMotiveFix", References.ENTITY, "minecraft:painting");
	}

	public Dynamic<?> fixTag(Dynamic<?> dynamic) {
		Optional<String> optional = dynamic.get("Motive").asString().result();
		if (optional.isPresent()) {
			String string = ((String)optional.get()).toLowerCase(Locale.ROOT);
			return dynamic.set("Motive", dynamic.createString(NamespacedSchema.ensureNamespaced((String)MAP.getOrDefault(string, string))));
		} else {
			return dynamic;
		}
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), this::fixTag);
	}
}
