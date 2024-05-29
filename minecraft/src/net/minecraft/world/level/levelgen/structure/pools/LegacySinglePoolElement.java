package net.minecraft.world.level.levelgen.structure.pools;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class LegacySinglePoolElement extends SinglePoolElement {
	public static final MapCodec<LegacySinglePoolElement> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(templateCodec(), processorsCodec(), projectionCodec(), overrideLiquidSettingsCodec())
				.apply(instance, LegacySinglePoolElement::new)
	);

	protected LegacySinglePoolElement(
		Either<ResourceLocation, StructureTemplate> either,
		Holder<StructureProcessorList> holder,
		StructureTemplatePool.Projection projection,
		Optional<LiquidSettings> optional
	) {
		super(either, holder, projection, optional);
	}

	@Override
	protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox boundingBox, LiquidSettings liquidSettings, boolean bl) {
		StructurePlaceSettings structurePlaceSettings = super.getSettings(rotation, boundingBox, liquidSettings, bl);
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
