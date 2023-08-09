package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class SinglePoolElement extends StructurePoolElement {
	private static final Codec<Either<ResourceLocation, StructureTemplate>> TEMPLATE_CODEC = Codec.of(
		SinglePoolElement::encodeTemplate, ResourceLocation.CODEC.map(Either::left)
	);
	public static final Codec<SinglePoolElement> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(templateCodec(), processorsCodec(), projectionCodec()).apply(instance, SinglePoolElement::new)
	);
	protected final Either<ResourceLocation, StructureTemplate> template;
	protected final Holder<StructureProcessorList> processors;

	private static <T> DataResult<T> encodeTemplate(Either<ResourceLocation, StructureTemplate> either, DynamicOps<T> dynamicOps, T object) {
		Optional<ResourceLocation> optional = either.left();
		return optional.isEmpty()
			? DataResult.error(() -> "Can not serialize a runtime pool element")
			: ResourceLocation.CODEC.encode((ResourceLocation)optional.get(), dynamicOps, object);
	}

	protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Holder<StructureProcessorList>> processorsCodec() {
		return StructureProcessorType.LIST_CODEC.fieldOf("processors").forGetter(singlePoolElement -> singlePoolElement.processors);
	}

	protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Either<ResourceLocation, StructureTemplate>> templateCodec() {
		return TEMPLATE_CODEC.fieldOf("location").forGetter(singlePoolElement -> singlePoolElement.template);
	}

	protected SinglePoolElement(
		Either<ResourceLocation, StructureTemplate> either, Holder<StructureProcessorList> holder, StructureTemplatePool.Projection projection
	) {
		super(projection);
		this.template = either;
		this.processors = holder;
	}

	@Override
	public Vec3i getSize(StructureTemplateManager structureTemplateManager, Rotation rotation) {
		StructureTemplate structureTemplate = this.getTemplate(structureTemplateManager);
		return structureTemplate.getSize(rotation);
	}

	private StructureTemplate getTemplate(StructureTemplateManager structureTemplateManager) {
		return this.template.map(structureTemplateManager::getOrCreate, Function.identity());
	}

	public List<StructureTemplate.StructureBlockInfo> getDataMarkers(
		StructureTemplateManager structureTemplateManager, BlockPos blockPos, Rotation rotation, boolean bl
	) {
		StructureTemplate structureTemplate = this.getTemplate(structureTemplateManager);
		List<StructureTemplate.StructureBlockInfo> list = structureTemplate.filterBlocks(
			blockPos, new StructurePlaceSettings().setRotation(rotation), Blocks.STRUCTURE_BLOCK, bl
		);
		List<StructureTemplate.StructureBlockInfo> list2 = Lists.<StructureTemplate.StructureBlockInfo>newArrayList();

		for (StructureTemplate.StructureBlockInfo structureBlockInfo : list) {
			CompoundTag compoundTag = structureBlockInfo.nbt();
			if (compoundTag != null) {
				StructureMode structureMode = StructureMode.valueOf(compoundTag.getString("mode"));
				if (structureMode == StructureMode.DATA) {
					list2.add(structureBlockInfo);
				}
			}
		}

		return list2;
	}

	@Override
	public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(
		StructureTemplateManager structureTemplateManager, BlockPos blockPos, Rotation rotation, RandomSource randomSource
	) {
		StructureTemplate structureTemplate = this.getTemplate(structureTemplateManager);
		ObjectArrayList<StructureTemplate.StructureBlockInfo> objectArrayList = structureTemplate.filterBlocks(
			blockPos, new StructurePlaceSettings().setRotation(rotation), Blocks.JIGSAW, true
		);
		Util.shuffle(objectArrayList, randomSource);
		return objectArrayList;
	}

	@Override
	public BoundingBox getBoundingBox(StructureTemplateManager structureTemplateManager, BlockPos blockPos, Rotation rotation) {
		StructureTemplate structureTemplate = this.getTemplate(structureTemplateManager);
		return structureTemplate.getBoundingBox(new StructurePlaceSettings().setRotation(rotation), blockPos);
	}

	@Override
	public boolean place(
		StructureTemplateManager structureTemplateManager,
		WorldGenLevel worldGenLevel,
		StructureManager structureManager,
		ChunkGenerator chunkGenerator,
		BlockPos blockPos,
		BlockPos blockPos2,
		Rotation rotation,
		BoundingBox boundingBox,
		RandomSource randomSource,
		boolean bl
	) {
		StructureTemplate structureTemplate = this.getTemplate(structureTemplateManager);
		StructurePlaceSettings structurePlaceSettings = this.getSettings(rotation, boundingBox, bl);
		if (!structureTemplate.placeInWorld(worldGenLevel, blockPos, blockPos2, structurePlaceSettings, randomSource, 18)) {
			return false;
		} else {
			for (StructureTemplate.StructureBlockInfo structureBlockInfo : StructureTemplate.processBlockInfos(
				worldGenLevel, blockPos, blockPos2, structurePlaceSettings, this.getDataMarkers(structureTemplateManager, blockPos, rotation, false)
			)) {
				this.handleDataMarker(worldGenLevel, structureBlockInfo, blockPos, rotation, randomSource, boundingBox);
			}

			return true;
		}
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

		this.processors.value().list().forEach(structurePlaceSettings::addProcessor);
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
