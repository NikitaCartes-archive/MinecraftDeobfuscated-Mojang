/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.LegacyStuffWrapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.FoliageColor;

@Environment(value=EnvType.CLIENT)
public class FoliageColorReloadListener
extends SimplePreparableReloadListener<int[]> {
    private static final ResourceLocation LOCATION = new ResourceLocation("textures/colormap/foliage.png");

    @Override
    protected int[] prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        try {
            return LegacyStuffWrapper.getPixels(resourceManager, LOCATION);
        } catch (IOException iOException) {
            throw new IllegalStateException("Failed to load foliage color texture", iOException);
        }
    }

    @Override
    protected void apply(int[] is, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        FoliageColor.init(is);
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }
}

