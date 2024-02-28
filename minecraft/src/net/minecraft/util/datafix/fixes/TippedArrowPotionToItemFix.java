package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class TippedArrowPotionToItemFix extends NamedEntityWriteReadFix {
	public TippedArrowPotionToItemFix(Schema schema) {
		super(schema, false, "TippedArrowPotionToItemFix", References.ENTITY, "minecraft:arrow");
	}

	@Override
	protected <T> Dynamic<T> fix(Dynamic<T> dynamic) {
		Optional<Dynamic<T>> optional = dynamic.get("Potion").result();
		Optional<Dynamic<T>> optional2 = dynamic.get("custom_potion_effects").result();
		Optional<Dynamic<T>> optional3 = dynamic.get("Color").result();
		return optional.isEmpty() && optional2.isEmpty() && optional3.isEmpty()
			? dynamic
			: dynamic.remove("Potion").remove("custom_potion_effects").remove("Color").update("item", dynamicx -> {
				Dynamic<?> dynamic2 = dynamicx.get("tag").orElseEmptyMap();
				if (optional.isPresent()) {
					dynamic2 = dynamic2.set("Potion", (Dynamic<?>)optional.get());
				}

				if (optional2.isPresent()) {
					dynamic2 = dynamic2.set("custom_potion_effects", (Dynamic<?>)optional2.get());
				}

				if (optional3.isPresent()) {
					dynamic2 = dynamic2.set("CustomPotionColor", (Dynamic<?>)optional3.get());
				}

				return dynamicx.set("tag", dynamic2);
			});
	}
}
