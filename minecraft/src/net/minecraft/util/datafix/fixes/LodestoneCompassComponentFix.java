package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class LodestoneCompassComponentFix extends ItemStackComponentRemainderFix {
	public LodestoneCompassComponentFix(Schema schema) {
		super(schema, "LodestoneCompassComponentFix", "minecraft:lodestone_target", "minecraft:lodestone_tracker");
	}

	@Override
	protected <T> Dynamic<T> fixComponent(Dynamic<T> dynamic) {
		Optional<Dynamic<T>> optional = dynamic.get("pos").result();
		Optional<Dynamic<T>> optional2 = dynamic.get("dimension").result();
		dynamic = dynamic.remove("pos").remove("dimension");
		if (optional.isPresent() && optional2.isPresent()) {
			dynamic = dynamic.set("target", dynamic.emptyMap().set("pos", (Dynamic<?>)optional.get()).set("dimension", (Dynamic<?>)optional2.get()));
		}

		return dynamic;
	}
}
