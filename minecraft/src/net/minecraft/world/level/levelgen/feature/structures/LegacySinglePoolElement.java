package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

public class LegacySinglePoolElement extends SinglePoolElement {
	@Deprecated
	public LegacySinglePoolElement(String string, List<StructureProcessor> list) {
		super(string, list, StructureTemplatePool.Projection.RIGID);
	}

	@Deprecated
	public LegacySinglePoolElement(String string) {
		super(string, ImmutableList.of());
	}

	public LegacySinglePoolElement(Dynamic<?> dynamic) {
		super(dynamic);
	}

	@Override
	protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox boundingBox, boolean bl) {
		StructurePlaceSettings structurePlaceSettings = super.getSettings(rotation, boundingBox, bl);
		structurePlaceSettings.popProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
		structurePlaceSettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
		return structurePlaceSettings;
	}

	@Override
	public StructurePoolElementType getType() {
		return StructurePoolElementType.LEGACY;
	}

	@Override
	public String toString() {
		return "LegacySingle[" + this.template + "]";
	}
}
