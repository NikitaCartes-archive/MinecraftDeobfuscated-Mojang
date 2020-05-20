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
import com.mojang.datafixers.types.templates.CompoundList;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class NewVillageFix
extends DataFix {
    public NewVillageFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        CompoundList.CompoundListType<String, ?> compoundListType = DSL.compoundList(DSL.string(), this.getInputSchema().getType(References.STRUCTURE_FEATURE));
        OpticFinder opticFinder = compoundListType.finder();
        return this.cap(compoundListType);
    }

    private <SF> TypeRewriteRule cap(CompoundList.CompoundListType<String, SF> compoundListType) {
        Type<?> type = this.getInputSchema().getType(References.CHUNK);
        Type<?> type2 = this.getInputSchema().getType(References.STRUCTURE_FEATURE);
        OpticFinder<?> opticFinder = type.findField("Level");
        OpticFinder<?> opticFinder2 = opticFinder.type().findField("Structures");
        OpticFinder<?> opticFinder3 = opticFinder2.type().findField("Starts");
        OpticFinder opticFinder4 = compoundListType.finder();
        return TypeRewriteRule.seq(this.fixTypeEverywhereTyped("NewVillageFix", type, typed2 -> typed2.updateTyped(opticFinder, typed -> typed.updateTyped(opticFinder2, typed2 -> typed2.updateTyped(opticFinder3, typed -> typed.update(opticFinder4, list -> list.stream().filter(pair -> !Objects.equals(pair.getFirst(), "Village")).map(pair -> pair.mapFirst(string -> string.equals("New_Village") ? "Village" : string)).collect(Collectors.toList()))).update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("References", dynamic -> {
            Optional<Dynamic<Dynamic>> optional = dynamic.get("New_Village").result();
            return DataFixUtils.orElse(optional.map(dynamic2 -> dynamic.remove("New_Village").set("Village", (Dynamic<?>)dynamic2)), dynamic).remove("Village");
        }))))), this.fixTypeEverywhereTyped("NewVillageStartFix", type2, typed -> typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("id", dynamic -> Objects.equals(NamespacedSchema.ensureNamespaced(dynamic.asString("")), "minecraft:new_village") ? dynamic.createString("minecraft:village") : dynamic))));
    }
}

