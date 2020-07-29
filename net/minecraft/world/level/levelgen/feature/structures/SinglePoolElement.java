/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElementType;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class SinglePoolElement
extends StructurePoolElement {
    private static final Codec<Either<ResourceLocation, StructureTemplate>> TEMPLATE_CODEC = Codec.of(SinglePoolElement::encodeTemplate, ResourceLocation.CODEC.map(Either::left));
    public static final Codec<SinglePoolElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(SinglePoolElement.templateCodec(), SinglePoolElement.processorsCodec(), SinglePoolElement.projectionCodec()).apply((Applicative)instance, SinglePoolElement::new));
    protected final Either<ResourceLocation, StructureTemplate> template;
    protected final Supplier<StructureProcessorList> processors;

    private static <T> DataResult<T> encodeTemplate(Either<ResourceLocation, StructureTemplate> either, DynamicOps<T> dynamicOps, T object) {
        Optional<ResourceLocation> optional = either.left();
        if (!optional.isPresent()) {
            return DataResult.error("Can not serialize a runtime pool element");
        }
        return ResourceLocation.CODEC.encode(optional.get(), dynamicOps, object);
    }

    protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Supplier<StructureProcessorList>> processorsCodec() {
        return ((MapCodec)StructureProcessorType.LIST_CODEC.fieldOf("processors")).forGetter(singlePoolElement -> singlePoolElement.processors);
    }

    protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Either<ResourceLocation, StructureTemplate>> templateCodec() {
        return ((MapCodec)TEMPLATE_CODEC.fieldOf("location")).forGetter(singlePoolElement -> singlePoolElement.template);
    }

    protected SinglePoolElement(Either<ResourceLocation, StructureTemplate> either, Supplier<StructureProcessorList> supplier, StructureTemplatePool.Projection projection) {
        super(projection);
        this.template = either;
        this.processors = supplier;
    }

    public SinglePoolElement(StructureTemplate structureTemplate) {
        this(Either.right(structureTemplate), () -> ProcessorLists.EMPTY, StructureTemplatePool.Projection.RIGID);
    }

    private StructureTemplate getTemplate(StructureManager structureManager) {
        return this.template.map(structureManager::getOrCreate, Function.identity());
    }

    public List<StructureTemplate.StructureBlockInfo> getDataMarkers(StructureManager structureManager, BlockPos blockPos, Rotation rotation, boolean bl) {
        StructureTemplate structureTemplate = this.getTemplate(structureManager);
        List<StructureTemplate.StructureBlockInfo> list = structureTemplate.filterBlocks(blockPos, new StructurePlaceSettings().setRotation(rotation), Blocks.STRUCTURE_BLOCK, bl);
        ArrayList<StructureTemplate.StructureBlockInfo> list2 = Lists.newArrayList();
        for (StructureTemplate.StructureBlockInfo structureBlockInfo : list) {
            StructureMode structureMode;
            if (structureBlockInfo.nbt == null || (structureMode = StructureMode.valueOf(structureBlockInfo.nbt.getString("mode"))) != StructureMode.DATA) continue;
            list2.add(structureBlockInfo);
        }
        return list2;
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureManager structureManager, BlockPos blockPos, Rotation rotation, Random random) {
        StructureTemplate structureTemplate = this.getTemplate(structureManager);
        List<StructureTemplate.StructureBlockInfo> list = structureTemplate.filterBlocks(blockPos, new StructurePlaceSettings().setRotation(rotation), Blocks.JIGSAW, true);
        Collections.shuffle(list, random);
        return list;
    }

    @Override
    public BoundingBox getBoundingBox(StructureManager structureManager, BlockPos blockPos, Rotation rotation) {
        StructureTemplate structureTemplate = this.getTemplate(structureManager);
        return structureTemplate.getBoundingBox(new StructurePlaceSettings().setRotation(rotation), blockPos);
    }

    @Override
    public boolean place(StructureManager structureManager, WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockPos blockPos2, Rotation rotation, BoundingBox boundingBox, Random random, boolean bl) {
        StructurePlaceSettings structurePlaceSettings;
        StructureTemplate structureTemplate = this.getTemplate(structureManager);
        if (structureTemplate.placeInWorld(worldGenLevel, blockPos, blockPos2, structurePlaceSettings = this.getSettings(rotation, boundingBox, bl), random, 18)) {
            List<StructureTemplate.StructureBlockInfo> list = StructureTemplate.processBlockInfos(worldGenLevel, blockPos, blockPos2, structurePlaceSettings, this.getDataMarkers(structureManager, blockPos, rotation, false));
            for (StructureTemplate.StructureBlockInfo structureBlockInfo : list) {
                this.handleDataMarker(worldGenLevel, structureBlockInfo, blockPos, rotation, random, boundingBox);
            }
            return true;
        }
        return false;
    }

    protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox boundingBox, boolean bl) {
        StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings();
        structurePlaceSettings.setBoundingBox(boundingBox);
        structurePlaceSettings.setRotation(rotation);
        structurePlaceSettings.setKnownShape(true);
        structurePlaceSettings.setIgnoreEntities(false);
        structurePlaceSettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        structurePlaceSettings.setFinalizeEntities(true);
        if (!bl) {
            structurePlaceSettings.addProcessor(JigsawReplacementProcessor.INSTANCE);
        }
        this.processors.get().list().forEach(structurePlaceSettings::addProcessor);
        this.getProjection().getProcessors().forEach(structurePlaceSettings::addProcessor);
        return structurePlaceSettings;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return StructurePoolElementType.SINGLE;
    }

    public String toString() {
        return "Single[" + this.template + "]";
    }
}

