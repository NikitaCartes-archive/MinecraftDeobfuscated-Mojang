package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;
import java.util.stream.Stream;

public class PoiTypeRenameFix extends AbstractPoiSectionFix {
	private final Function<String, String> renamer;

	public PoiTypeRenameFix(Schema schema, String string, Function<String, String> function) {
		super(schema, string);
		this.renamer = function;
	}

	@Override
	protected <T> Stream<Dynamic<T>> processRecords(Stream<Dynamic<T>> stream) {
		return stream.map(
			dynamic -> dynamic.update("type", dynamicx -> DataFixUtils.orElse(dynamicx.asString().map(this.renamer).map(dynamicx::createString).result(), dynamicx))
		);
	}
}
