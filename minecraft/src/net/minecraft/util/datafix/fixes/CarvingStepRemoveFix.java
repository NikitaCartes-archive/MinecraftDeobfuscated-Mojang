package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class CarvingStepRemoveFix extends DataFix {
	public CarvingStepRemoveFix(Schema schema) {
		super(schema, false);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped("CarvingStepRemoveFix", this.getInputSchema().getType(References.CHUNK), CarvingStepRemoveFix::fixChunk);
	}

	private static Typed<?> fixChunk(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), dynamic -> {
			Dynamic<?> dynamic2 = dynamic;
			Optional<? extends Dynamic<?>> optional = dynamic.get("CarvingMasks").result();
			if (optional.isPresent()) {
				Optional<? extends Dynamic<?>> optional2 = ((Dynamic)optional.get()).get("AIR").result();
				if (optional2.isPresent()) {
					dynamic2 = dynamic.set("carving_mask", (Dynamic<?>)optional2.get());
				}
			}

			return dynamic2.remove("CarvingMasks");
		});
	}
}
