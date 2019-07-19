package net.minecraft.util.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class OminousBannerBlockEntityRenameFix extends NamedEntityFix {
	public OminousBannerBlockEntityRenameFix(Schema schema, boolean bl) {
		super(schema, bl, "OminousBannerBlockEntityRenameFix", References.BLOCK_ENTITY, "minecraft:banner");
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), this::fixTag);
	}

	private Dynamic<?> fixTag(Dynamic<?> dynamic) {
		Optional<String> optional = dynamic.get("CustomName").asString();
		if (optional.isPresent()) {
			String string = (String)optional.get();
			string = string.replace("\"translate\":\"block.minecraft.illager_banner\"", "\"translate\":\"block.minecraft.ominous_banner\"");
			return dynamic.set("CustomName", dynamic.createString(string));
		} else {
			return dynamic;
		}
	}
}
