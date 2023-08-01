package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Streams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class BlockEntitySignDoubleSidedEditableTextFix extends NamedEntityFix {
	public static final String FILTERED_CORRECT = "_filtered_correct";
	private static final String DEFAULT_COLOR = "black";
	private static final String EMPTY_COMPONENT = Component.Serializer.toJson(CommonComponents.EMPTY);

	public BlockEntitySignDoubleSidedEditableTextFix(Schema schema, String string, String string2) {
		super(schema, false, string, References.BLOCK_ENTITY, string2);
	}

	private static <T> Dynamic<T> fixTag(Dynamic<T> dynamic) {
		return dynamic.set("front_text", fixFrontTextTag(dynamic)).set("back_text", createDefaultText(dynamic)).set("is_waxed", dynamic.createBoolean(false));
	}

	private static <T> Dynamic<T> fixFrontTextTag(Dynamic<T> dynamic) {
		Dynamic<T> dynamic2 = dynamic.createString(EMPTY_COMPONENT);
		List<Dynamic<T>> list = getLines(dynamic, "Text").map(optional -> (Dynamic)optional.orElse(dynamic2)).toList();
		Dynamic<T> dynamic3 = dynamic.emptyMap()
			.set("messages", dynamic.createList(list.stream()))
			.set("color", (Dynamic<?>)dynamic.get("Color").result().orElse(dynamic.createString("black")))
			.set("has_glowing_text", (Dynamic<?>)dynamic.get("GlowingText").result().orElse(dynamic.createBoolean(false)))
			.set("_filtered_correct", dynamic.createBoolean(true));
		List<Optional<Dynamic<T>>> list2 = getLines(dynamic, "FilteredText").toList();
		if (list2.stream().anyMatch(Optional::isPresent)) {
			dynamic3 = dynamic3.set("filtered_messages", dynamic.createList(Streams.mapWithIndex(list2.stream(), (optional, l) -> {
				Dynamic<T> dynamicx = (Dynamic<T>)list.get((int)l);
				return (Dynamic<?>)optional.orElse(dynamicx);
			})));
		}

		return dynamic3;
	}

	private static <T> Stream<Optional<Dynamic<T>>> getLines(Dynamic<T> dynamic, String string) {
		return Stream.of(
			dynamic.get(string + "1").result(), dynamic.get(string + "2").result(), dynamic.get(string + "3").result(), dynamic.get(string + "4").result()
		);
	}

	private static <T> Dynamic<T> createDefaultText(Dynamic<T> dynamic) {
		return dynamic.emptyMap()
			.set("messages", createEmptyLines(dynamic))
			.set("color", dynamic.createString("black"))
			.set("has_glowing_text", dynamic.createBoolean(false));
	}

	private static <T> Dynamic<T> createEmptyLines(Dynamic<T> dynamic) {
		Dynamic<T> dynamic2 = dynamic.createString(EMPTY_COMPONENT);
		return dynamic.createList(Stream.of(dynamic2, dynamic2, dynamic2, dynamic2));
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), BlockEntitySignDoubleSidedEditableTextFix::fixTag);
	}
}
