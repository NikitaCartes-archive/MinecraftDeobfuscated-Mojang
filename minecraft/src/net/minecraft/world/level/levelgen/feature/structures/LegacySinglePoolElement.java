package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class LegacySinglePoolElement extends SinglePoolElement {
	public static final Codec<LegacySinglePoolElement> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(templateCodec(), processorsCodec(), projectionCodec()).apply(instance, LegacySinglePoolElement::new)
	);

	@Deprecated
	public LegacySinglePoolElement(String string, List<StructureProcessor> list) {
		super(string, list);
	}

	private LegacySinglePoolElement(Either<ResourceLocation, StructureTemplate> either, List<StructureProcessor> list, StructureTemplatePool.Projection projection) {
		super(either, list, projection);
	}

	@Deprecated
	public LegacySinglePoolElement(String string) {
		super(string, ImmutableList.of());
	}

	@Override
	protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox boundingBox, boolean bl) {
		StructurePlaceSettings structurePlaceSettings = super.getSettings(rotation, boundingBox, bl);
		structurePlaceSettings.popProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
		structurePlaceSettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
		return structurePlaceSettings;
	}

	@Override
	public StructurePoolElementType<?> getType() {
		return StructurePoolElementType.LEGACY;
	}

	@Override
	public String toString() {
		return "LegacySingle[" + this.template + "]";
	}
}
