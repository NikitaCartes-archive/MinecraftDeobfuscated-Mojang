/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.datafix.fixes.References;

public class ItemLoreFix
extends DataFix {
    public ItemLoreFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<?> opticFinder = type.findField("tag");
        return this.fixTypeEverywhereTyped("Item Lore componentize", type, typed2 -> typed2.updateTyped(opticFinder, typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.update("display", dynamic2 -> dynamic2.update("Lore", dynamic -> DataFixUtils.orElse(dynamic.asStreamOpt().map(ItemLoreFix::fixLoreList).map(dynamic::createList), dynamic))))));
    }

    private static <T> Stream<Dynamic<T>> fixLoreList(Stream<Dynamic<T>> stream) {
        return stream.map(dynamic -> DataFixUtils.orElse(dynamic.asString().map(ItemLoreFix::fixLoreEntry).map(dynamic::createString), dynamic));
    }

    private static String fixLoreEntry(String string) {
        return Component.Serializer.toJson(new TextComponent(string));
    }
}

