/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Deserializer;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.NopProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class SinglePoolElement
extends StructurePoolElement {
    protected final Either<ResourceLocation, StructureTemplate> template;
    protected final ImmutableList<StructureProcessor> processors;

    @Deprecated
    public SinglePoolElement(String string, List<StructureProcessor> list) {
        this(string, list, StructureTemplatePool.Projection.RIGID);
    }

    public SinglePoolElement(String string, List<StructureProcessor> list, StructureTemplatePool.Projection projection) {
        super(projection);
        this.template = Either.left(new ResourceLocation(string));
        this.processors = ImmutableList.copyOf(list);
    }

    public SinglePoolElement(StructureTemplate structureTemplate, List<StructureProcessor> list, StructureTemplatePool.Projection projection) {
        super(projection);
        this.template = Either.right(structureTemplate);
        this.processors = ImmutableList.copyOf(list);
    }

    @Deprecated
    public SinglePoolElement(String string) {
        this(string, ImmutableList.of());
    }

    public SinglePoolElement(Dynamic<?> dynamic2) {
        super(dynamic2);
        this.template = Either.left(new ResourceLocation(dynamic2.get("location").asString("")));
        this.processors = ImmutableList.copyOf(dynamic2.get("processors").asList(dynamic -> Deserializer.deserialize(dynamic, Registry.STRUCTURE_PROCESSOR, "processor_type", NopProcessor.INSTANCE)));
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
        if (structureTemplate.placeInWorld(worldGenLevel, blockPos, blockPos2, structurePlaceSettings = this.getSettings(rotation, boundingBox, bl), 18)) {
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
        this.processors.forEach(structurePlaceSettings::addProcessor);
        this.getProjection().getProcessors().forEach(structurePlaceSettings::addProcessor);
        return structurePlaceSettings;
    }

    @Override
    public StructurePoolElementType getType() {
        return StructurePoolElementType.SINGLE;
    }

    @Override
    public <T> Dynamic<T> getDynamic(DynamicOps<T> dynamicOps) {
        return new Dynamic<Object>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("location"), dynamicOps.createString(this.template.left().orElseThrow(RuntimeException::new).toString()), dynamicOps.createString("processors"), dynamicOps.createList(this.processors.stream().map(structureProcessor -> structureProcessor.serialize(dynamicOps).getValue())))));
    }

    public String toString() {
        return "Single[" + this.template + "]";
    }
}

