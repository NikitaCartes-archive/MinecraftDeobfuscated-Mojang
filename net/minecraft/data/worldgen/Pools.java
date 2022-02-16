/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.BastionPieces;
import net.minecraft.data.worldgen.PillagerOutpostPools;
import net.minecraft.data.worldgen.VillagePools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class Pools {
    public static final ResourceKey<StructureTemplatePool> EMPTY = ResourceKey.create(Registry.TEMPLATE_POOL_REGISTRY, new ResourceLocation("empty"));
    private static final Holder<StructureTemplatePool> BUILTIN_EMPTY = Pools.register(new StructureTemplatePool(EMPTY.location(), EMPTY.location(), ImmutableList.of(), StructureTemplatePool.Projection.RIGID));

    public static Holder<StructureTemplatePool> register(StructureTemplatePool structureTemplatePool) {
        return BuiltinRegistries.register(BuiltinRegistries.TEMPLATE_POOL, structureTemplatePool.getName(), structureTemplatePool);
    }

    public static Holder<StructureTemplatePool> bootstrap() {
        BastionPieces.bootstrap();
        PillagerOutpostPools.bootstrap();
        VillagePools.bootstrap();
        return BUILTIN_EMPTY;
    }

    static {
        Pools.bootstrap();
    }
}

