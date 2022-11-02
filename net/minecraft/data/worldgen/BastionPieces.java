/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BastionBridgePools;
import net.minecraft.data.worldgen.BastionHoglinStablePools;
import net.minecraft.data.worldgen.BastionHousingUnitsPools;
import net.minecraft.data.worldgen.BastionSharedPools;
import net.minecraft.data.worldgen.BastionTreasureRoomPools;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class BastionPieces {
    public static final ResourceKey<StructureTemplatePool> START = Pools.createKey("bastion/starts");

    public static void bootstrap(BootstapContext<StructureTemplatePool> bootstapContext) {
        HolderGetter<StructureProcessorList> holderGetter = bootstapContext.lookup(Registry.PROCESSOR_LIST_REGISTRY);
        Holder.Reference<StructureProcessorList> holder = holderGetter.getOrThrow(ProcessorLists.BASTION_GENERIC_DEGRADATION);
        HolderGetter<StructureTemplatePool> holderGetter2 = bootstapContext.lookup(Registry.TEMPLATE_POOL_REGISTRY);
        Holder.Reference<StructureTemplatePool> holder2 = holderGetter2.getOrThrow(Pools.EMPTY);
        bootstapContext.register(START, new StructureTemplatePool(holder2, ImmutableList.of(Pair.of(StructurePoolElement.single("bastion/units/air_base", holder), 1), Pair.of(StructurePoolElement.single("bastion/hoglin_stable/air_base", holder), 1), Pair.of(StructurePoolElement.single("bastion/treasure/big_air_full", holder), 1), Pair.of(StructurePoolElement.single("bastion/bridge/starting_pieces/entrance_base", holder), 1)), StructureTemplatePool.Projection.RIGID));
        BastionHousingUnitsPools.bootstrap(bootstapContext);
        BastionHoglinStablePools.bootstrap(bootstapContext);
        BastionTreasureRoomPools.bootstrap(bootstapContext);
        BastionBridgePools.bootstrap(bootstapContext);
        BastionSharedPools.bootstrap(bootstapContext);
    }
}

