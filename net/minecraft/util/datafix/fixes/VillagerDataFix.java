/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class VillagerDataFix
extends NamedEntityFix {
    public VillagerDataFix(Schema schema, String string) {
        super(schema, false, "Villager profession data fix (" + string + ")", References.ENTITY, string);
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        Dynamic dynamic = typed.get(DSL.remainderFinder());
        return typed.set(DSL.remainderFinder(), dynamic.remove("Profession").remove("Career").remove("CareerLevel").set("VillagerData", dynamic.createMap(ImmutableMap.of(dynamic.createString("type"), dynamic.createString("minecraft:plains"), dynamic.createString("profession"), dynamic.createString(VillagerDataFix.upgradeData(dynamic.get("Profession").asInt(0), dynamic.get("Career").asInt(0))), dynamic.createString("level"), DataFixUtils.orElse(dynamic.get("CareerLevel").get(), dynamic.createInt(1))))));
    }

    private static String upgradeData(int i, int j) {
        if (i == 0) {
            if (j == 2) {
                return "minecraft:fisherman";
            }
            if (j == 3) {
                return "minecraft:shepherd";
            }
            if (j == 4) {
                return "minecraft:fletcher";
            }
            return "minecraft:farmer";
        }
        if (i == 1) {
            if (j == 2) {
                return "minecraft:cartographer";
            }
            return "minecraft:librarian";
        }
        if (i == 2) {
            return "minecraft:cleric";
        }
        if (i == 3) {
            if (j == 2) {
                return "minecraft:weaponsmith";
            }
            if (j == 3) {
                return "minecraft:toolsmith";
            }
            return "minecraft:armorer";
        }
        if (i == 4) {
            if (j == 2) {
                return "minecraft:leatherworker";
            }
            return "minecraft:butcher";
        }
        if (i == 5) {
            return "minecraft:nitwit";
        }
        return "minecraft:none";
    }
}

