/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;
import java.util.Objects;
import net.minecraft.util.datafix.fixes.SimplestEntityRenameFix;

public class EntityZombifiedPiglinRenameFix
extends SimplestEntityRenameFix {
    public static final Map<String, String> RENAMED_IDS = ImmutableMap.builder().put("minecraft:zombie_pigman_spawn_egg", "minecraft:zombified_piglin_spawn_egg").build();

    public EntityZombifiedPiglinRenameFix(Schema schema) {
        super("EntityZombifiedPiglinRenameFix", schema, true);
    }

    @Override
    protected String rename(String string) {
        return Objects.equals("minecraft:zombie_pigman", string) ? "minecraft:zombified_piglin" : string;
    }
}

