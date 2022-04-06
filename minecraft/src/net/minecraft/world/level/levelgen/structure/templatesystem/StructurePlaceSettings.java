package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class StructurePlaceSettings {
	private Mirror mirror = Mirror.NONE;
	private Rotation rotation = Rotation.NONE;
	private BlockPos rotationPivot = BlockPos.ZERO;
	private boolean ignoreEntities;
	@Nullable
	private BoundingBox boundingBox;
	private boolean keepLiquids = true;
	@Nullable
	private RandomSource random;
	private int palette;
	private final List<StructureProcessor> processors = Lists.<StructureProcessor>newArrayList();
	private boolean knownShape;
	private boolean finalizeEntities;

	public StructurePlaceSettings copy() {
		StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings();
		structurePlaceSettings.mirror = this.mirror;
		structurePlaceSettings.rotation = this.rotation;
		structurePlaceSettings.rotationPivot = this.rotationPivot;
		structurePlaceSettings.ignoreEntities = this.ignoreEntities;
		structurePlaceSettings.boundingBox = this.boundingBox;
		structurePlaceSettings.keepLiquids = this.keepLiquids;
		structurePlaceSettings.random = this.random;
		structurePlaceSettings.palette = this.palette;
		structurePlaceSettings.processors.addAll(this.processors);
		structurePlaceSettings.knownShape = this.knownShape;
		structurePlaceSettings.finalizeEntities = this.finalizeEntities;
		return structurePlaceSettings;
	}

	public StructurePlaceSettings setMirror(Mirror mirror) {
		this.mirror = mirror;
		return this;
	}

	public StructurePlaceSettings setRotation(Rotation rotation) {
		this.rotation = rotation;
		return this;
	}

	public StructurePlaceSettings setRotationPivot(BlockPos blockPos) {
		this.rotationPivot = blockPos;
		return this;
	}

	public StructurePlaceSettings setIgnoreEntities(boolean bl) {
		this.ignoreEntities = bl;
		return this;
	}

	public StructurePlaceSettings setBoundingBox(BoundingBox boundingBox) {
		this.boundingBox = boundingBox;
		return this;
	}

	public StructurePlaceSettings setRandom(@Nullable RandomSource randomSource) {
		this.random = randomSource;
		return this;
	}

	public StructurePlaceSettings setKeepLiquids(boolean bl) {
		this.keepLiquids = bl;
		return this;
	}

	public StructurePlaceSettings setKnownShape(boolean bl) {
		this.knownShape = bl;
		return this;
	}

	public StructurePlaceSettings clearProcessors() {
		this.processors.clear();
		return this;
	}

	public StructurePlaceSettings addProcessor(StructureProcessor structureProcessor) {
		this.processors.add(structureProcessor);
		return this;
	}

	public StructurePlaceSettings popProcessor(StructureProcessor structureProcessor) {
		this.processors.remove(structureProcessor);
		return this;
	}

	public Mirror getMirror() {
		return this.mirror;
	}

	public Rotation getRotation() {
		return this.rotation;
	}

	public BlockPos getRotationPivot() {
		return this.rotationPivot;
	}

	public RandomSource getRandom(@Nullable BlockPos blockPos) {
		if (this.random != null) {
			return this.random;
		} else {
			return blockPos == null ? RandomSource.create(Util.getMillis()) : RandomSource.create(Mth.getSeed(blockPos));
		}
	}

	public boolean isIgnoreEntities() {
		return this.ignoreEntities;
	}

	@Nullable
	public BoundingBox getBoundingBox() {
		return this.boundingBox;
	}

	public boolean getKnownShape() {
		return this.knownShape;
	}

	public List<StructureProcessor> getProcessors() {
		return this.processors;
	}

	public boolean shouldKeepLiquids() {
		return this.keepLiquids;
	}

	public StructureTemplate.Palette getRandomPalette(List<StructureTemplate.Palette> list, @Nullable BlockPos blockPos) {
		int i = list.size();
		if (i == 0) {
			throw new IllegalStateException("No palettes");
		} else {
			return (StructureTemplate.Palette)list.get(this.getRandom(blockPos).nextInt(i));
		}
	}

	public StructurePlaceSettings setFinalizeEntities(boolean bl) {
		this.finalizeEntities = bl;
		return this;
	}

	public boolean shouldFinalizeEntities() {
		return this.finalizeEntities;
	}
}
