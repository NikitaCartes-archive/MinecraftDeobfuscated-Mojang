/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public class FossilFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<FossilFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ResourceLocation.CODEC.listOf().fieldOf("fossil_structures")).forGetter(fossilFeatureConfiguration -> fossilFeatureConfiguration.fossilStructures), ((MapCodec)ResourceLocation.CODEC.listOf().fieldOf("overlay_structures")).forGetter(fossilFeatureConfiguration -> fossilFeatureConfiguration.overlayStructures), ((MapCodec)StructureProcessorType.LIST_CODEC.fieldOf("fossil_processors")).forGetter(fossilFeatureConfiguration -> fossilFeatureConfiguration.fossilProcessors), ((MapCodec)StructureProcessorType.LIST_CODEC.fieldOf("overlay_processors")).forGetter(fossilFeatureConfiguration -> fossilFeatureConfiguration.overlayProcessors), ((MapCodec)Codec.intRange(0, 7).fieldOf("max_empty_corners_allowed")).forGetter(fossilFeatureConfiguration -> fossilFeatureConfiguration.maxEmptyCornersAllowed)).apply((Applicative<FossilFeatureConfiguration, ?>)instance, FossilFeatureConfiguration::new));
    public final List<ResourceLocation> fossilStructures;
    public final List<ResourceLocation> overlayStructures;
    public final Supplier<StructureProcessorList> fossilProcessors;
    public final Supplier<StructureProcessorList> overlayProcessors;
    public final int maxEmptyCornersAllowed;

    public FossilFeatureConfiguration(List<ResourceLocation> list, List<ResourceLocation> list2, Supplier<StructureProcessorList> supplier, Supplier<StructureProcessorList> supplier2, int i) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Fossil structure lists need at least one entry");
        }
        if (list.size() != list2.size()) {
            throw new IllegalArgumentException("Fossil structure lists must be equal lengths");
        }
        this.fossilStructures = list;
        this.overlayStructures = list2;
        this.fossilProcessors = supplier;
        this.overlayProcessors = supplier2;
        this.maxEmptyCornersAllowed = i;
    }

    public FossilFeatureConfiguration(List<ResourceLocation> list, List<ResourceLocation> list2, StructureProcessorList structureProcessorList, StructureProcessorList structureProcessorList2, int i) {
        this(list, list2, () -> structureProcessorList, () -> structureProcessorList2, i);
    }
}

