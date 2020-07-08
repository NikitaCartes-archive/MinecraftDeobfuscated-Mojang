/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.BastionPieces;
import net.minecraft.data.worldgen.PillagerOutpostPools;
import net.minecraft.data.worldgen.VillagePools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;

public class Pools {
    public static final StructureTemplatePool EMPTY = Pools.register(new StructureTemplatePool(new ResourceLocation("empty"), new ResourceLocation("empty"), ImmutableList.of(), StructureTemplatePool.Projection.RIGID));
    public static final StructureTemplatePool INVALID = Pools.register(new StructureTemplatePool(new ResourceLocation("invalid"), new ResourceLocation("invalid"), ImmutableList.of(), StructureTemplatePool.Projection.RIGID));

    public static StructureTemplatePool register(StructureTemplatePool structureTemplatePool) {
        return BuiltinRegistries.register(BuiltinRegistries.TEMPLATE_POOL, structureTemplatePool.getName(), structureTemplatePool);
    }

    public static void bootstrap() {
        BastionPieces.bootstrap();
        PillagerOutpostPools.bootstrap();
        VillagePools.bootstrap();
    }

    static {
        Pools.bootstrap();
    }
}

