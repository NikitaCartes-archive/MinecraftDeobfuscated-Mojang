package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;

public class PrimedTntBlockStateFixer extends NamedEntityWriteReadFix {
	public PrimedTntBlockStateFixer(Schema schema) {
		super(schema, true, "PrimedTnt BlockState fixer", References.ENTITY, "minecraft:tnt");
	}

	private static <T> Dynamic<T> renameFuse(Dynamic<T> dynamic) {
		Optional<Dynamic<T>> optional = dynamic.get("Fuse").get().result();
		return optional.isPresent() ? dynamic.set("fuse", (Dynamic<?>)optional.get()) : dynamic;
	}

	private static <T> Dynamic<T> insertBlockState(Dynamic<T> dynamic) {
		return dynamic.set("block_state", dynamic.createMap(Map.of(dynamic.createString("Name"), dynamic.createString("minecraft:tnt"))));
	}

	@Override
	protected <T> Dynamic<T> fix(Dynamic<T> dynamic) {
		return renameFuse(insertBlockState(dynamic));
	}
}
