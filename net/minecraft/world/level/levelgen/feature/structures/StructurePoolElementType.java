/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.structures;

import net.minecraft.core.Registry;
import net.minecraft.util.Deserializer;
import net.minecraft.world.level.levelgen.feature.structures.EmptyPoolElement;
import net.minecraft.world.level.levelgen.feature.structures.FeaturePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.ListPoolElement;
import net.minecraft.world.level.levelgen.feature.structures.SinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;

public interface StructurePoolElementType
extends Deserializer<StructurePoolElement> {
    public static final StructurePoolElementType SINGLE = StructurePoolElementType.register("single_pool_element", SinglePoolElement::new);
    public static final StructurePoolElementType LIST = StructurePoolElementType.register("list_pool_element", ListPoolElement::new);
    public static final StructurePoolElementType FEATURE = StructurePoolElementType.register("feature_pool_element", FeaturePoolElement::new);
    public static final StructurePoolElementType EMPTY = StructurePoolElementType.register("empty_pool_element", dynamic -> EmptyPoolElement.INSTANCE);
    public static final StructurePoolElementType LEGACY = StructurePoolElementType.register("legacy_single_pool_element", LegacySinglePoolElement::new);

    public static StructurePoolElementType register(String string, StructurePoolElementType structurePoolElementType) {
        return Registry.register(Registry.STRUCTURE_POOL_ELEMENT, string, structurePoolElementType);
    }
}

