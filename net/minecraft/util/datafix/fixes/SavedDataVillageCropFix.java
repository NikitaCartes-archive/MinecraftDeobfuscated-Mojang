/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.util.datafix.fixes.References;

public class SavedDataVillageCropFix
extends DataFix {
    public SavedDataVillageCropFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.writeFixAndRead("SavedDataVillageCropFix", this.getInputSchema().getType(References.STRUCTURE_FEATURE), this.getOutputSchema().getType(References.STRUCTURE_FEATURE), this::fixTag);
    }

    private <T> Dynamic<T> fixTag(Dynamic<T> dynamic) {
        return dynamic.update("Children", SavedDataVillageCropFix::updateChildren);
    }

    private static <T> Dynamic<T> updateChildren(Dynamic<T> dynamic) {
        return dynamic.asStreamOpt().map(SavedDataVillageCropFix::updateChildren).map(dynamic::createList).result().orElse(dynamic);
    }

    private static Stream<? extends Dynamic<?>> updateChildren(Stream<? extends Dynamic<?>> stream) {
        return stream.map(dynamic -> {
            String string = dynamic.get("id").asString("");
            if ("ViF".equals(string)) {
                return SavedDataVillageCropFix.updateSingleField(dynamic);
            }
            if ("ViDF".equals(string)) {
                return SavedDataVillageCropFix.updateDoubleField(dynamic);
            }
            return dynamic;
        });
    }

    private static <T> Dynamic<T> updateSingleField(Dynamic<T> dynamic) {
        dynamic = SavedDataVillageCropFix.updateCrop(dynamic, "CA");
        return SavedDataVillageCropFix.updateCrop(dynamic, "CB");
    }

    private static <T> Dynamic<T> updateDoubleField(Dynamic<T> dynamic) {
        dynamic = SavedDataVillageCropFix.updateCrop(dynamic, "CA");
        dynamic = SavedDataVillageCropFix.updateCrop(dynamic, "CB");
        dynamic = SavedDataVillageCropFix.updateCrop(dynamic, "CC");
        return SavedDataVillageCropFix.updateCrop(dynamic, "CD");
    }

    private static <T> Dynamic<T> updateCrop(Dynamic<T> dynamic, String string) {
        if (dynamic.get(string).asNumber().result().isPresent()) {
            return dynamic.set(string, BlockStateData.getTag(dynamic.get(string).asInt(0) << 4));
        }
        return dynamic;
    }
}

