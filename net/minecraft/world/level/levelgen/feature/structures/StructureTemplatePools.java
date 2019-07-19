/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;

public class StructureTemplatePools {
    private final Map<ResourceLocation, StructureTemplatePool> pools = Maps.newHashMap();

    public StructureTemplatePools() {
        this.register(StructureTemplatePool.EMPTY);
    }

    public void register(StructureTemplatePool structureTemplatePool) {
        this.pools.put(structureTemplatePool.getName(), structureTemplatePool);
    }

    public StructureTemplatePool getPool(ResourceLocation resourceLocation) {
        StructureTemplatePool structureTemplatePool = this.pools.get(resourceLocation);
        return structureTemplatePool != null ? structureTemplatePool : StructureTemplatePool.INVALID;
    }
}

