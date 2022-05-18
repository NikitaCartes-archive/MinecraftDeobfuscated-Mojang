package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PoiTypeRemoveFix extends AbstractPoiSectionFix {
	private final Predicate<String> typesToKeep;

	public PoiTypeRemoveFix(Schema schema, String string, Predicate<String> predicate) {
		super(schema, string);
		this.typesToKeep = predicate.negate();
	}

	@Override
	protected <T> Stream<Dynamic<T>> processRecords(Stream<Dynamic<T>> stream) {
		return stream.filter(this::shouldKeepRecord);
	}

	private <T> boolean shouldKeepRecord(Dynamic<T> dynamic) {
		return dynamic.get("type").asString().result().filter(this.typesToKeep).isPresent();
	}
}
