package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Deserializer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.NopProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class SinglePoolElement extends StructurePoolElement {
	protected final ResourceLocation location;
	protected final ImmutableList<StructureProcessor> processors;

	@Deprecated
	public SinglePoolElement(String string, List<StructureProcessor> list) {
		this(string, list, StructureTemplatePool.Projection.RIGID);
	}

	public SinglePoolElement(String string, List<StructureProcessor> list, StructureTemplatePool.Projection projection) {
		super(projection);
		this.location = new ResourceLocation(string);
		this.processors = ImmutableList.copyOf(list);
	}

	@Deprecated
	public SinglePoolElement(String string) {
		this(string, ImmutableList.of());
	}

	public SinglePoolElement(Dynamic<?> dynamic) {
		super(dynamic);
		this.location = new ResourceLocation(dynamic.get("location").asString(""));
		this.processors = ImmutableList.copyOf(
			dynamic.get("processors").asList(dynamicx -> Deserializer.deserialize(dynamicx, Registry.STRUCTURE_PROCESSOR, "processor_type", NopProcessor.INSTANCE))
		);
	}

	public List<StructureTemplate.StructureBlockInfo> getDataMarkers(StructureManager structureManager, BlockPos blockPos, Rotation rotation, boolean bl) {
		StructureTemplate structureTemplate = structureManager.getOrCreate(this.location);
		List<StructureTemplate.StructureBlockInfo> list = structureTemplate.filterBlocks(
			blockPos, new StructurePlaceSettings().setRotation(rotation), Blocks.STRUCTURE_BLOCK, bl
		);
		List<StructureTemplate.StructureBlockInfo> list2 = Lists.<StructureTemplate.StructureBlockInfo>newArrayList();

		for (StructureTemplate.StructureBlockInfo structureBlockInfo : list) {
			if (structureBlockInfo.nbt != null) {
				StructureMode structureMode = StructureMode.valueOf(structureBlockInfo.nbt.getString("mode"));
				if (structureMode == StructureMode.DATA) {
					list2.add(structureBlockInfo);
				}
			}
		}

		return list2;
	}

	@Override
	public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(
		StructureManager structureManager, BlockPos blockPos, Rotation rotation, Random random
	) {
		StructureTemplate structureTemplate = structureManager.getOrCreate(this.location);
		List<StructureTemplate.StructureBlockInfo> list = structureTemplate.filterBlocks(
			blockPos, new StructurePlaceSettings().setRotation(rotation), Blocks.JIGSAW, true
		);
		Collections.shuffle(list, random);
		return list;
	}

	@Override
	public BoundingBox getBoundingBox(StructureManager structureManager, BlockPos blockPos, Rotation rotation) {
		StructureTemplate structureTemplate = structureManager.getOrCreate(this.location);
		return structureTemplate.getBoundingBox(new StructurePlaceSettings().setRotation(rotation), blockPos);
	}

	@Override
	public boolean place(
		StructureManager structureManager,
		LevelAccessor levelAccessor,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator<?> chunkGenerator,
		BlockPos blockPos,
		BlockPos blockPos2,
		Rotation rotation,
		BoundingBox boundingBox,
		Random random
	) {
		StructureTemplate structureTemplate = structureManager.getOrCreate(this.location);
		StructurePlaceSettings structurePlaceSettings = this.getSettings(rotation, boundingBox);
		if (!structureTemplate.placeInWorld(levelAccessor, blockPos, blockPos2, structurePlaceSettings, 18)) {
			return false;
		} else {
			for (StructureTemplate.StructureBlockInfo structureBlockInfo : StructureTemplate.processBlockInfos(
				levelAccessor, blockPos, blockPos2, structurePlaceSettings, this.getDataMarkers(structureManager, blockPos, rotation, false)
			)) {
				this.handleDataMarker(levelAccessor, structureBlockInfo, blockPos, rotation, random, boundingBox);
			}

			return true;
		}
	}

	protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox boundingBox) {
		StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings();
		structurePlaceSettings.setBoundingBox(boundingBox);
		structurePlaceSettings.setRotation(rotation);
		structurePlaceSettings.setKnownShape(true);
		structurePlaceSettings.setIgnoreEntities(false);
		structurePlaceSettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
		structurePlaceSettings.addProcessor(JigsawReplacementProcessor.INSTANCE);
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
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("location"),
					dynamicOps.createString(this.location.toString()),
					dynamicOps.createString("processors"),
					dynamicOps.createList(this.processors.stream().map(structureProcessor -> structureProcessor.serialize(dynamicOps).getValue()))
				)
			)
		);
	}

	public String toString() {
		return "Single[" + this.location + "]";
	}
}
