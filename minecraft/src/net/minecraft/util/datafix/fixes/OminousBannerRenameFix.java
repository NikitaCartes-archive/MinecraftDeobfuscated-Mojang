package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class OminousBannerRenameFix extends ItemStackTagFix {
	public OminousBannerRenameFix(Schema schema) {
		super(schema, "OminousBannerRenameFix", string -> string.equals("minecraft:white_banner"));
	}

	@Override
	protected <T> Dynamic<T> fixItemStackTag(Dynamic<T> dynamic) {
		Optional<? extends Dynamic<?>> optional = dynamic.get("display").result();
		if (optional.isPresent()) {
			Dynamic<?> dynamic2 = (Dynamic<?>)optional.get();
			Optional<String> optional2 = dynamic2.get("Name").asString().result();
			if (optional2.isPresent()) {
				String string = (String)optional2.get();
				string = string.replace("\"translate\":\"block.minecraft.illager_banner\"", "\"translate\":\"block.minecraft.ominous_banner\"");
				dynamic2 = dynamic2.set("Name", dynamic2.createString(string));
			}

			return dynamic.set("display", dynamic2);
		} else {
			return dynamic;
		}
	}
}
