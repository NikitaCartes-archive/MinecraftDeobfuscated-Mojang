package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Streams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.ComponentDataFixUtils;

public class DropInvalidSignDataFix extends NamedEntityFix {
	private static final String[] FIELDS_TO_DROP = new String[]{
		"Text1", "Text2", "Text3", "Text4", "FilteredText1", "FilteredText2", "FilteredText3", "FilteredText4", "Color", "GlowingText"
	};

	public DropInvalidSignDataFix(Schema schema, String string, String string2) {
		super(schema, false, string, References.BLOCK_ENTITY, string2);
	}

	private static <T> Dynamic<T> fix(Dynamic<T> dynamic) {
		dynamic = dynamic.update("front_text", DropInvalidSignDataFix::fixText);
		dynamic = dynamic.update("back_text", DropInvalidSignDataFix::fixText);

		for (String string : FIELDS_TO_DROP) {
			dynamic = dynamic.remove(string);
		}

		return dynamic;
	}

	private static <T> Dynamic<T> fixText(Dynamic<T> dynamic) {
		boolean bl = dynamic.get("_filtered_correct").asBoolean(false);
		if (bl) {
			return dynamic.remove("_filtered_correct");
		} else {
			Optional<Stream<Dynamic<T>>> optional = dynamic.get("filtered_messages").asStreamOpt().result();
			if (optional.isEmpty()) {
				return dynamic;
			} else {
				Dynamic<T> dynamic2 = ComponentDataFixUtils.createEmptyComponent(dynamic.getOps());
				List<Dynamic<T>> list = ((Stream)dynamic.get("messages").asStreamOpt().result().orElse(Stream.of())).toList();
				List<Dynamic<T>> list2 = Streams.<Dynamic, Dynamic<T>>mapWithIndex((Stream<Dynamic>)optional.get(), (dynamic2x, l) -> {
					Dynamic<T> dynamic3 = l < (long)list.size() ? (Dynamic)list.get((int)l) : dynamic2;
					return dynamic2x.equals(dynamic2) ? dynamic3 : dynamic2x;
				}).toList();
				return list2.stream().allMatch(dynamic2x -> dynamic2x.equals(dynamic2))
					? dynamic.remove("filtered_messages")
					: dynamic.set("filtered_messages", dynamic.createList(list2.stream()));
			}
		}
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), DropInvalidSignDataFix::fix);
	}
}
