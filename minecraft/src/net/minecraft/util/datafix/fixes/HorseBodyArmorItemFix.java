package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class HorseBodyArmorItemFix extends NamedEntityWriteReadFix {
	private final String previousBodyArmorTag;

	public HorseBodyArmorItemFix(Schema schema, String string, String string2) {
		super(schema, true, "Horse armor fix for " + string, References.ENTITY, string);
		this.previousBodyArmorTag = string2;
	}

	@Override
	protected <T> Dynamic<T> fix(Dynamic<T> dynamic) {
		Optional<? extends Dynamic<?>> optional = dynamic.get(this.previousBodyArmorTag).result();
		if (optional.isPresent()) {
			Dynamic<?> dynamic2 = (Dynamic<?>)optional.get();
			Dynamic<T> dynamic3 = dynamic.remove(this.previousBodyArmorTag);
			dynamic3 = dynamic3.set("body_armor_item", dynamic2);
			return dynamic3.set("body_armor_drop_chance", dynamic.createFloat(2.0F));
		} else {
			return dynamic;
		}
	}
}
