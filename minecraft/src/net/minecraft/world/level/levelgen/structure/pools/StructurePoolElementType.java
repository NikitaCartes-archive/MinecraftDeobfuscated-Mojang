package net.minecraft.world.level.levelgen.structure.pools;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public interface StructurePoolElementType<P extends StructurePoolElement> {
	StructurePoolElementType<SinglePoolElement> SINGLE = register("single_pool_element", SinglePoolElement.CODEC);
	StructurePoolElementType<ListPoolElement> LIST = register("list_pool_element", ListPoolElement.CODEC);
	StructurePoolElementType<FeaturePoolElement> FEATURE = register("feature_pool_element", FeaturePoolElement.CODEC);
	StructurePoolElementType<EmptyPoolElement> EMPTY = register("empty_pool_element", EmptyPoolElement.CODEC);
	StructurePoolElementType<LegacySinglePoolElement> LEGACY = register("legacy_single_pool_element", LegacySinglePoolElement.CODEC);

	MapCodec<P> codec();

	static <P extends StructurePoolElement> StructurePoolElementType<P> register(String string, MapCodec<P> mapCodec) {
		return Registry.register(BuiltInRegistries.STRUCTURE_POOL_ELEMENT, string, () -> mapCodec);
	}
}
