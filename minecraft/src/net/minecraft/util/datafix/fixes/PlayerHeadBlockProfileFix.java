package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class PlayerHeadBlockProfileFix extends NamedEntityFix {
	public PlayerHeadBlockProfileFix(Schema schema) {
		super(schema, false, "PlayerHeadBlockProfileFix", References.BLOCK_ENTITY, "minecraft:skull");
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), this::fix);
	}

	private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
		Optional<Dynamic<T>> optional = dynamic.get("SkullOwner").result();
		Optional<Dynamic<T>> optional2 = dynamic.get("ExtraType").result();
		Optional<Dynamic<T>> optional3 = optional.or(() -> optional2);
		if (optional3.isEmpty()) {
			return dynamic;
		} else {
			dynamic = dynamic.remove("SkullOwner").remove("ExtraType");
			return dynamic.set("profile", ItemStackComponentizationFix.fixProfile((Dynamic<?>)optional3.get()));
		}
	}
}
