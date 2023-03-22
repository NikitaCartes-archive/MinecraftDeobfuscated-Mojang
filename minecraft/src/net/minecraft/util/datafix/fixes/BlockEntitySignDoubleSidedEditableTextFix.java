package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class BlockEntitySignDoubleSidedEditableTextFix extends NamedEntityFix {
	public BlockEntitySignDoubleSidedEditableTextFix(Schema schema, String string, String string2) {
		super(schema, false, string, References.BLOCK_ENTITY, string2);
	}

	private static Dynamic<?> fixTag(Dynamic<?> dynamic) {
		String string = "black";
		Dynamic<?> dynamic2 = dynamic.emptyMap();
		dynamic2 = dynamic2.set("messages", getTextList(dynamic, "Text"));
		dynamic2 = dynamic2.set("filtered_messages", getTextList(dynamic, "FilteredText"));
		Optional<? extends Dynamic<?>> optional = dynamic.get("Color").result();
		dynamic2 = dynamic2.set("color", optional.isPresent() ? (Dynamic)optional.get() : dynamic2.createString("black"));
		Optional<? extends Dynamic<?>> optional2 = dynamic.get("GlowingText").result();
		dynamic2 = dynamic2.set("has_glowing_text", optional2.isPresent() ? (Dynamic)optional2.get() : dynamic2.createBoolean(false));
		Dynamic<?> dynamic3 = dynamic.emptyMap();
		Dynamic<?> dynamic4 = getEmptyTextList(dynamic);
		dynamic3 = dynamic3.set("messages", dynamic4);
		dynamic3 = dynamic3.set("filtered_messages", dynamic4);
		dynamic3 = dynamic3.set("color", dynamic3.createString("black"));
		dynamic3 = dynamic3.set("has_glowing_text", dynamic3.createBoolean(false));
		dynamic = dynamic.set("front_text", dynamic2);
		return dynamic.set("back_text", dynamic3);
	}

	private static <T> Dynamic<T> getTextList(Dynamic<T> dynamic, String string) {
		Dynamic<T> dynamic2 = dynamic.createString(getEmptyComponent());
		return dynamic.createList(
			Stream.of(
				(Dynamic)dynamic.get(string + "1").result().orElse(dynamic2),
				(Dynamic)dynamic.get(string + "2").result().orElse(dynamic2),
				(Dynamic)dynamic.get(string + "3").result().orElse(dynamic2),
				(Dynamic)dynamic.get(string + "4").result().orElse(dynamic2)
			)
		);
	}

	private static <T> Dynamic<T> getEmptyTextList(Dynamic<T> dynamic) {
		Dynamic<T> dynamic2 = dynamic.createString(getEmptyComponent());
		return dynamic.createList(Stream.of(dynamic2, dynamic2, dynamic2, dynamic2));
	}

	private static String getEmptyComponent() {
		return Component.Serializer.toJson(CommonComponents.EMPTY);
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), BlockEntitySignDoubleSidedEditableTextFix::fixTag);
	}
}
