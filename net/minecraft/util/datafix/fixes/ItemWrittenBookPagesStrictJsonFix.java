/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.util.datafix.fixes.BlockEntitySignTextStrictJsonFix;
import net.minecraft.util.datafix.fixes.References;
import org.apache.commons.lang3.StringUtils;

public class ItemWrittenBookPagesStrictJsonFix
extends DataFix {
    public ItemWrittenBookPagesStrictJsonFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    public Dynamic<?> fixTag(Dynamic<?> dynamic) {
        return dynamic.update("pages", dynamic2 -> DataFixUtils.orElse(dynamic2.asStreamOpt().map(stream -> stream.map(dynamic -> {
            if (!dynamic.asString().result().isPresent()) {
                return dynamic;
            }
            String string = dynamic.asString("");
            Component component = null;
            if ("null".equals(string) || StringUtils.isEmpty(string)) {
                component = CommonComponents.EMPTY;
            } else if (string.charAt(0) == '\"' && string.charAt(string.length() - 1) == '\"' || string.charAt(0) == '{' && string.charAt(string.length() - 1) == '}') {
                try {
                    component = GsonHelper.fromNullableJson(BlockEntitySignTextStrictJsonFix.GSON, string, Component.class, true);
                    if (component == null) {
                        component = CommonComponents.EMPTY;
                    }
                } catch (Exception exception) {
                    // empty catch block
                }
                if (component == null) {
                    try {
                        component = Component.Serializer.fromJson(string);
                    } catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (component == null) {
                    try {
                        component = Component.Serializer.fromJsonLenient(string);
                    } catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (component == null) {
                    component = Component.literal(string);
                }
            } else {
                component = Component.literal(string);
            }
            return dynamic.createString(Component.Serializer.toJson(component));
        })).map(dynamic::createList).result(), dynamic.emptyList()));
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<?> opticFinder = type.findField("tag");
        return this.fixTypeEverywhereTyped("ItemWrittenBookPagesStrictJsonFix", type, typed2 -> typed2.updateTyped(opticFinder, typed -> typed.update(DSL.remainderFinder(), this::fixTag)));
    }
}

