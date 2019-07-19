package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.stream.Stream;

public class SavedDataVillageCropFix extends DataFix {
	public SavedDataVillageCropFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	public TypeRewriteRule makeRule() {
		return this.writeFixAndRead(
			"SavedDataVillageCropFix",
			this.getInputSchema().getType(References.STRUCTURE_FEATURE),
			this.getOutputSchema().getType(References.STRUCTURE_FEATURE),
			this::fixTag
		);
	}

	private <T> Dynamic<T> fixTag(Dynamic<T> dynamic) {
		return dynamic.update("Children", SavedDataVillageCropFix::updateChildren);
	}

	private static <T> Dynamic<T> updateChildren(Dynamic<T> dynamic) {
		return (Dynamic<T>)dynamic.asStreamOpt().map(SavedDataVillageCropFix::updateChildren).map(dynamic::createList).orElse(dynamic);
	}

	private static Stream<? extends Dynamic<?>> updateChildren(Stream<? extends Dynamic<?>> stream) {
		return stream.map(dynamic -> {
			String string = dynamic.get("id").asString("");
			if ("ViF".equals(string)) {
				return updateSingleField(dynamic);
			} else {
				return "ViDF".equals(string) ? updateDoubleField(dynamic) : dynamic;
			}
		});
	}

	private static <T> Dynamic<T> updateSingleField(Dynamic<T> dynamic) {
		dynamic = updateCrop(dynamic, "CA");
		return updateCrop(dynamic, "CB");
	}

	private static <T> Dynamic<T> updateDoubleField(Dynamic<T> dynamic) {
		dynamic = updateCrop(dynamic, "CA");
		dynamic = updateCrop(dynamic, "CB");
		dynamic = updateCrop(dynamic, "CC");
		return updateCrop(dynamic, "CD");
	}

	private static <T> Dynamic<T> updateCrop(Dynamic<T> dynamic, String string) {
		return dynamic.get(string).asNumber().isPresent() ? dynamic.set(string, BlockStateData.getTag(dynamic.get(string).asInt(0) << 4)) : dynamic;
	}
}
