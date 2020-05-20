/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class JigsawRotationFix
extends DataFix {
    private static final Map<String, String> renames = ImmutableMap.builder().put("down", "down_south").put("up", "up_north").put("north", "north_up").put("south", "south_up").put("west", "west_up").put("east", "east_up").build();

    public JigsawRotationFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    private static Dynamic<?> fix(Dynamic<?> dynamic2) {
        Optional<String> optional = dynamic2.get("Name").asString().result();
        if (optional.equals(Optional.of("minecraft:jigsaw"))) {
            return dynamic2.update("Properties", dynamic -> {
                String string = dynamic.get("facing").asString("north");
                return dynamic.remove("facing").set("orientation", dynamic.createString(renames.getOrDefault(string, string)));
            });
        }
        return dynamic2;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("jigsaw_rotation_fix", this.getInputSchema().getType(References.BLOCK_STATE), typed -> typed.update(DSL.remainderFinder(), JigsawRotationFix::fix));
    }
}

