/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.DesertVillagePools;
import net.minecraft.data.worldgen.PlainVillagePools;
import net.minecraft.data.worldgen.SavannaVillagePools;
import net.minecraft.data.worldgen.SnowyVillagePools;
import net.minecraft.data.worldgen.TaigaVillagePools;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class VillagePools {
    public static void bootstrap(BootstapContext<StructureTemplatePool> bootstapContext) {
        PlainVillagePools.bootstrap(bootstapContext);
        SnowyVillagePools.bootstrap(bootstapContext);
        SavannaVillagePools.bootstrap(bootstapContext);
        DesertVillagePools.bootstrap(bootstapContext);
        TaigaVillagePools.bootstrap(bootstapContext);
    }
}

