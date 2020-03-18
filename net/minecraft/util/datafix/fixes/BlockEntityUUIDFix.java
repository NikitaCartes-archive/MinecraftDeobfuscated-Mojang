/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.AbstractUUIDFix;
import net.minecraft.util.datafix.fixes.References;

public class BlockEntityUUIDFix
extends AbstractUUIDFix {
    public BlockEntityUUIDFix(Schema schema) {
        super(schema, References.BLOCK_ENTITY);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("BlockEntityUUIDFix", this.getInputSchema().getType(this.typeReference), typed -> {
            typed = this.updateNamedChoice((Typed<?>)typed, "minecraft:conduit", this::updateConduit);
            typed = this.updateNamedChoice((Typed<?>)typed, "minecraft:skull", this::updateSkull);
            return typed;
        });
    }

    private Dynamic<?> updateSkull(Dynamic<?> dynamic3) {
        return dynamic3.get("Owner").get().map(dynamic -> BlockEntityUUIDFix.replaceUUIDString(dynamic, "Id", "Id").orElse((Dynamic<?>)dynamic)).map(dynamic2 -> dynamic3.remove("Owner").set("SkullOwner", (Dynamic<?>)dynamic2)).orElse(dynamic3);
    }

    private Dynamic<?> updateConduit(Dynamic<?> dynamic) {
        return BlockEntityUUIDFix.replaceUUIDMLTag(dynamic, "target_uuid", "Target").orElse(dynamic);
    }
}

