/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.PoiTypeRename;

public class BeehivePoiRenameFix
extends PoiTypeRename {
    public BeehivePoiRenameFix(Schema schema) {
        super(schema, false);
    }

    @Override
    protected String rename(String string) {
        return string.equals("minecraft:bee_hive") ? "minecraft:beehive" : string;
    }
}

