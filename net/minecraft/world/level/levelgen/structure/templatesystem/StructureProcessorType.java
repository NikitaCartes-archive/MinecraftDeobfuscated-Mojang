/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlackstoneReplaceProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockAgeProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.LavaSubmergedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.NopProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

public interface StructureProcessorType<P extends StructureProcessor> {
    public static final StructureProcessorType<BlockIgnoreProcessor> BLOCK_IGNORE = StructureProcessorType.register("block_ignore", BlockIgnoreProcessor.CODEC);
    public static final StructureProcessorType<BlockRotProcessor> BLOCK_ROT = StructureProcessorType.register("block_rot", BlockRotProcessor.CODEC);
    public static final StructureProcessorType<GravityProcessor> GRAVITY = StructureProcessorType.register("gravity", GravityProcessor.CODEC);
    public static final StructureProcessorType<JigsawReplacementProcessor> JIGSAW_REPLACEMENT = StructureProcessorType.register("jigsaw_replacement", JigsawReplacementProcessor.CODEC);
    public static final StructureProcessorType<RuleProcessor> RULE = StructureProcessorType.register("rule", RuleProcessor.CODEC);
    public static final StructureProcessorType<NopProcessor> NOP = StructureProcessorType.register("nop", NopProcessor.CODEC);
    public static final StructureProcessorType<BlockAgeProcessor> BLOCK_AGE = StructureProcessorType.register("block_age", BlockAgeProcessor.CODEC);
    public static final StructureProcessorType<BlackstoneReplaceProcessor> BLACKSTONE_REPLACE = StructureProcessorType.register("blackstone_replace", BlackstoneReplaceProcessor.CODEC);
    public static final StructureProcessorType<LavaSubmergedBlockProcessor> LAVA_SUBMERGED_BLOCK = StructureProcessorType.register("lava_submerged_block", LavaSubmergedBlockProcessor.CODEC);
    public static final Codec<StructureProcessor> SINGLE_CODEC = Registry.STRUCTURE_PROCESSOR.dispatch("processor_type", StructureProcessor::getType, StructureProcessorType::codec);
    public static final MapCodec<ImmutableList<StructureProcessor>> DIRECT_CODEC = StructureProcessorType.handleDefaultField("processors", SINGLE_CODEC.listOf().xmap(ImmutableList::copyOf, Function.identity()));
    public static final Codec<Supplier<ImmutableList<StructureProcessor>>> LIST_CODEC = RegistryFileCodec.create(Registry.PROCESSOR_LIST_REGISTRY, DIRECT_CODEC);

    public Codec<P> codec();

    public static <E> MapCodec<E> handleDefaultField(String string, Codec<E> codec) {
        MapEncoder mapCodec = codec.fieldOf(string);
        return new MapCodec<E>((MapCodec)mapCodec, string){
            final /* synthetic */ MapCodec val$parent;
            final /* synthetic */ String val$defaultFieldName;
            {
                this.val$parent = mapCodec;
                this.val$defaultFieldName = string;
            }

            @Override
            public <O> Stream<O> keys(DynamicOps<O> dynamicOps) {
                return this.val$parent.keys(dynamicOps);
            }

            @Override
            public <O> DataResult<E> decode(DynamicOps<O> dynamicOps, MapLike<O> mapLike) {
                return this.val$parent.decode(dynamicOps, mapLike);
            }

            @Override
            public <O> RecordBuilder<O> encode(E object, DynamicOps<O> dynamicOps, RecordBuilder<O> recordBuilder) {
                return this.val$parent.encode(object, dynamicOps, recordBuilder);
            }

            @Override
            public Codec<E> codec() {
                final Codec codec = super.codec();
                return new Codec<E>(){

                    @Override
                    public <O> DataResult<Pair<E, O>> decode(DynamicOps<O> dynamicOps, O object) {
                        if (dynamicOps.compressMaps()) {
                            return codec.decode(dynamicOps, object);
                        }
                        DataResult<MapLike<O>> dataResult = dynamicOps.getMap(object);
                        MapLike mapLike2 = dataResult.get().map(mapLike -> mapLike, partialResult -> MapLike.forMap(ImmutableMap.of(dynamicOps.createString(val$defaultFieldName), object), dynamicOps));
                        return val$parent.decode(dynamicOps, mapLike2).map((? super R object2) -> Pair.of(object2, object));
                    }

                    @Override
                    public <O> DataResult<O> encode(E object, DynamicOps<O> dynamicOps, O object2) {
                        return codec.encode(object, dynamicOps, object2);
                    }
                };
            }
        };
    }

    public static <P extends StructureProcessor> StructureProcessorType<P> register(String string, Codec<P> codec) {
        return Registry.register(Registry.STRUCTURE_PROCESSOR, string, () -> codec);
    }
}

