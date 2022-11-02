/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidTagsProvider
extends IntrinsicHolderTagsProvider<Fluid> {
    public FluidTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(packOutput, Registry.FLUID_REGISTRY, completableFuture, fluid -> fluid.builtInRegistryHolder().key());
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)FluidTags.WATER)).add(Fluids.WATER, Fluids.FLOWING_WATER);
        ((IntrinsicHolderTagsProvider.IntrinsicTagAppender)this.tag((TagKey)FluidTags.LAVA)).add(Fluids.LAVA, Fluids.FLOWING_LAVA);
    }
}

