/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;
import net.minecraft.util.datafix.fixes.SimplestEntityRenameFix;

public class EntityCodSalmonFix
extends SimplestEntityRenameFix {
    public static final Map<String, String> RENAMED_IDS = ImmutableMap.builder().put("minecraft:salmon_mob", "minecraft:salmon").put("minecraft:cod_mob", "minecraft:cod").build();
    public static final Map<String, String> RENAMED_EGG_IDS = ImmutableMap.builder().put("minecraft:salmon_mob_spawn_egg", "minecraft:salmon_spawn_egg").put("minecraft:cod_mob_spawn_egg", "minecraft:cod_spawn_egg").build();

    public EntityCodSalmonFix(Schema schema, boolean bl) {
        super("EntityCodSalmonFix", schema, bl);
    }

    @Override
    protected String rename(String string) {
        return RENAMED_IDS.getOrDefault(string, string);
    }
}

