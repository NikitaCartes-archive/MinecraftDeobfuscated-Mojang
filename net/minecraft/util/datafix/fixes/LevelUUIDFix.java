/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.AbstractUUIDFix;
import net.minecraft.util.datafix.fixes.References;

public class LevelUUIDFix
extends AbstractUUIDFix {
    public LevelUUIDFix(Schema schema) {
        super(schema, References.LEVEL);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("LevelUUIDFix", this.getInputSchema().getType(this.typeReference), typed2 -> typed2.updateTyped(DSL.remainderFinder(), typed -> typed.update(DSL.remainderFinder(), dynamic -> {
            dynamic = this.updateCustomBossEvents((Dynamic<?>)dynamic);
            dynamic = this.updateDragonFight((Dynamic<?>)dynamic);
            dynamic = this.updateWanderingTrader((Dynamic<?>)dynamic);
            return dynamic;
        })));
    }

    private Dynamic<?> updateWanderingTrader(Dynamic<?> dynamic) {
        return LevelUUIDFix.replaceUUIDString(dynamic, "WanderingTraderId", "WanderingTraderId").orElse(dynamic);
    }

    private Dynamic<?> updateDragonFight(Dynamic<?> dynamic2) {
        return dynamic2.update("DimensionData", dynamic -> dynamic.updateMapValues(pair -> pair.mapSecond(dynamic2 -> dynamic2.update("DragonFight", dynamic -> LevelUUIDFix.replaceUUIDLeastMost(dynamic, "DragonUUID", "Dragon").orElse((Dynamic<?>)dynamic)))));
    }

    private Dynamic<?> updateCustomBossEvents(Dynamic<?> dynamic2) {
        return dynamic2.update("CustomBossEvents", dynamic -> dynamic.updateMapValues(pair -> pair.mapSecond(dynamic -> dynamic.update("Players", dynamic22 -> dynamic.createList(dynamic22.asStream().map(dynamic -> LevelUUIDFix.createUUIDFromML(dynamic).orElseGet(() -> {
            LOGGER.warn("CustomBossEvents contains invalid UUIDs.");
            return dynamic;
        })))))));
    }
}

