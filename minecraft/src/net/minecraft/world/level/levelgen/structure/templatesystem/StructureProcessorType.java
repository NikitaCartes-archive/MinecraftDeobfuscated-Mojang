package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;

public interface StructureProcessorType<P extends StructureProcessor> {
	StructureProcessorType<BlockIgnoreProcessor> BLOCK_IGNORE = register("block_ignore", BlockIgnoreProcessor.CODEC);
	StructureProcessorType<BlockRotProcessor> BLOCK_ROT = register("block_rot", BlockRotProcessor.CODEC);
	StructureProcessorType<GravityProcessor> GRAVITY = register("gravity", GravityProcessor.CODEC);
	StructureProcessorType<JigsawReplacementProcessor> JIGSAW_REPLACEMENT = register("jigsaw_replacement", JigsawReplacementProcessor.CODEC);
	StructureProcessorType<RuleProcessor> RULE = register("rule", RuleProcessor.CODEC);
	StructureProcessorType<NopProcessor> NOP = register("nop", NopProcessor.CODEC);
	StructureProcessorType<BlockAgeProcessor> BLOCK_AGE = register("block_age", BlockAgeProcessor.CODEC);
	StructureProcessorType<BlackstoneReplaceProcessor> BLACKSTONE_REPLACE = register("blackstone_replace", BlackstoneReplaceProcessor.CODEC);
	StructureProcessorType<LavaSubmergedBlockProcessor> LAVA_SUBMERGED_BLOCK = register("lava_submerged_block", LavaSubmergedBlockProcessor.CODEC);
	Codec<StructureProcessor> SINGLE_CODEC = Registry.STRUCTURE_PROCESSOR.dispatch("processor_type", StructureProcessor::getType, StructureProcessorType::codec);
	MapCodec<ImmutableList<StructureProcessor>> DIRECT_CODEC = handleDefaultField(
		"processors", SINGLE_CODEC.listOf().xmap(ImmutableList::copyOf, Function.identity())
	);
	Codec<Supplier<ImmutableList<StructureProcessor>>> LIST_CODEC = RegistryFileCodec.create(Registry.PROCESSOR_LIST_REGISTRY, DIRECT_CODEC);

	Codec<P> codec();

	static <E> MapCodec<E> handleDefaultField(String string, Codec<E> codec) {
		final MapCodec<E> mapCodec = codec.fieldOf(string);
		return new MapCodec<E>() {
			@Override
			public <O> Stream<O> keys(DynamicOps<O> dynamicOps) {
				return mapCodec.keys(dynamicOps);
			}

			@Override
			public <O> DataResult<E> decode(DynamicOps<O> dynamicOps, MapLike<O> mapLike) {
				return mapCodec.decode(dynamicOps, mapLike);
			}

			@Override
			public <O> RecordBuilder<O> encode(E object, DynamicOps<O> dynamicOps, RecordBuilder<O> recordBuilder) {
				return mapCodec.encode(object, dynamicOps, recordBuilder);
			}

			@Override
			public Codec<E> codec() {
				final Codec<E> codec = super.codec();
				return new Codec<E>() {
					@Override
					public <O> DataResult<Pair<E, O>> decode(DynamicOps<O> dynamicOps, O object) {
						if (dynamicOps.compressMaps()) {
							return codec.decode(dynamicOps, object);
						} else {
							DataResult<MapLike<O>> dataResult = dynamicOps.getMap(object);
							MapLike<O> mapLike = dataResult.get()
								.map(mapLikex -> mapLikex, partialResult -> MapLike.forMap(ImmutableMap.of(dynamicOps.createString(string), object), dynamicOps));
							return mapCodec.decode(dynamicOps, mapLike).map(object2 -> Pair.of(object2, object));
						}
					}

					@Override
					public <O> DataResult<O> encode(E object, DynamicOps<O> dynamicOps, O object2) {
						return codec.encode(object, dynamicOps, object2);
					}
				};
			}
		};
	}

	static <P extends StructureProcessor> StructureProcessorType<P> register(String string, Codec<P> codec) {
		return Registry.register(Registry.STRUCTURE_PROCESSOR, string, () -> codec);
	}
}
