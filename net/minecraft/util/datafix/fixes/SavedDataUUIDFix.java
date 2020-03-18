/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.AbstractUUIDFix;
import net.minecraft.util.datafix.fixes.References;

public class SavedDataUUIDFix
extends AbstractUUIDFix {
    public SavedDataUUIDFix(Schema schema) {
        super(schema, References.SAVED_DATA);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("SavedDataUUIDFix", this.getInputSchema().getType(this.typeReference), typed2 -> typed2.updateTyped(typed2.getType().findField("data"), typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.update("Raids", dynamic2 -> dynamic2.createList(dynamic2.asStream().map(dynamic -> dynamic.update("HeroesOfTheVillage", dynamic2 -> dynamic2.createList(dynamic2.asStream().map(dynamic -> SavedDataUUIDFix.createUUIDFromLongs(dynamic, "UUIDMost", "UUIDLeast").orElseGet(() -> {
            LOGGER.warn("HeroesOfTheVillage contained invalid UUIDs.");
            return dynamic;
        }))))))))));
    }
}

