package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.StringUtils;

public class ItemWrittenBookPagesStrictJsonFix extends DataFix {
	public ItemWrittenBookPagesStrictJsonFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	public Dynamic<?> fixTag(Dynamic<?> dynamic) {
		return dynamic.update("pages", dynamic2 -> DataFixUtils.orElse(dynamic2.asStreamOpt().map(stream -> stream.map(dynamicxx -> {
					if (!dynamicxx.asString().result().isPresent()) {
						return dynamicxx;
					} else {
						String string = dynamicxx.asString("");
						Component component = null;
						if (!"null".equals(string) && !StringUtils.isEmpty(string)) {
							if (string.charAt(0) == '"' && string.charAt(string.length() - 1) == '"' || string.charAt(0) == '{' && string.charAt(string.length() - 1) == '}') {
								try {
									component = GsonHelper.fromNullableJson(BlockEntitySignTextStrictJsonFix.GSON, string, Component.class, true);
									if (component == null) {
										component = CommonComponents.EMPTY;
									}
								} catch (Exception var6) {
								}

								if (component == null) {
									try {
										component = Component.Serializer.fromJson(string);
									} catch (Exception var5) {
									}
								}

								if (component == null) {
									try {
										component = Component.Serializer.fromJsonLenient(string);
									} catch (Exception var4) {
									}
								}

								if (component == null) {
									component = Component.literal(string);
								}
							} else {
								component = Component.literal(string);
							}
						} else {
							component = CommonComponents.EMPTY;
						}

						return dynamicxx.createString(Component.Serializer.toJson(component));
					}
				})).map(dynamic::createList).result(), dynamic.emptyList()));
	}

	@Override
	public TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
		OpticFinder<?> opticFinder = type.findField("tag");
		return this.fixTypeEverywhereTyped(
			"ItemWrittenBookPagesStrictJsonFix", type, typed -> typed.updateTyped(opticFinder, typedx -> typedx.update(DSL.remainderFinder(), this::fixTag))
		);
	}
}
