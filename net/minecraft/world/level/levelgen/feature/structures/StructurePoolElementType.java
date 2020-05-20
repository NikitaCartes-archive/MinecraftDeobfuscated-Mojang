/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.structures.EmptyPoolElement;
import net.minecraft.world.level.levelgen.feature.structures.FeaturePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.ListPoolElement;
import net.minecraft.world.level.levelgen.feature.structures.SinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;

public interface StructurePoolElementType<P extends StructurePoolElement> {
    public static final StructurePoolElementType<SinglePoolElement> SINGLE = StructurePoolElementType.register("single_pool_element", SinglePoolElement.CODEC);
    public static final StructurePoolElementType<ListPoolElement> LIST = StructurePoolElementType.register("list_pool_element", ListPoolElement.CODEC);
    public static final StructurePoolElementType<FeaturePoolElement> FEATURE = StructurePoolElementType.register("feature_pool_element", FeaturePoolElement.CODEC);
    public static final StructurePoolElementType<EmptyPoolElement> EMPTY = StructurePoolElementType.register("empty_pool_element", EmptyPoolElement.CODEC);
    public static final StructurePoolElementType<LegacySinglePoolElement> LEGACY = StructurePoolElementType.register("legacy_single_pool_element", LegacySinglePoolElement.CODEC);

    public Codec<P> codec();

    public static <P extends StructurePoolElement> StructurePoolElementType<P> register(String string, Codec<P> codec) {
        return Registry.register(Registry.STRUCTURE_POOL_ELEMENT, string, () -> codec);
    }
}

