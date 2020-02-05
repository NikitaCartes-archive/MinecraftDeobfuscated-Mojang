/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.Set;
import net.minecraft.util.datafix.fixes.References;

public class WallPropertyFix
extends DataFix {
    private static final Set<String> WALL_BLOCKS = ImmutableSet.of("minecraft:andesite_wall", "minecraft:brick_wall", "minecraft:cobblestone_wall", "minecraft:diorite_wall", "minecraft:end_stone_brick_wall", "minecraft:granite_wall", new String[]{"minecraft:mossy_cobblestone_wall", "minecraft:mossy_stone_brick_wall", "minecraft:nether_brick_wall", "minecraft:prismarine_wall", "minecraft:red_nether_brick_wall", "minecraft:red_sandstone_wall", "minecraft:sandstone_wall", "minecraft:stone_brick_wall"});

    public WallPropertyFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("WallPropertyFix", this.getInputSchema().getType(References.BLOCK_STATE), typed -> typed.update(DSL.remainderFinder(), WallPropertyFix::upgradeBlockStateTag));
    }

    private static String mapProperty(String string) {
        return "true".equals(string) ? "low" : "none";
    }

    private static <T> Dynamic<T> fixWallProperty(Dynamic<T> dynamic2, String string) {
        return dynamic2.update(string, dynamic -> DataFixUtils.orElse(dynamic.asString().map(WallPropertyFix::mapProperty).map(dynamic::createString), dynamic));
    }

    private static <T> Dynamic<T> upgradeBlockStateTag(Dynamic<T> dynamic2) {
        boolean bl = dynamic2.get("Name").asString().filter(WALL_BLOCKS::contains).isPresent();
        if (!bl) {
            return dynamic2;
        }
        return dynamic2.update("Properties", dynamic -> {
            Dynamic dynamic2 = WallPropertyFix.fixWallProperty(dynamic, "east");
            dynamic2 = WallPropertyFix.fixWallProperty(dynamic2, "west");
            dynamic2 = WallPropertyFix.fixWallProperty(dynamic2, "north");
            return WallPropertyFix.fixWallProperty(dynamic2, "south");
        });
    }
}

