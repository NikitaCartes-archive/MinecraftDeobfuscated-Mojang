package net.minecraft.data.structures;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class StructureUpdater implements SnbtToNbt.Filter {
	@Override
	public CompoundTag apply(String string, CompoundTag compoundTag) {
		return string.startsWith("data/minecraft/structures/") ? updateStructure(patchVersion(compoundTag)) : compoundTag;
	}

	private static CompoundTag patchVersion(CompoundTag compoundTag) {
		if (!compoundTag.contains("DataVersion", 99)) {
			compoundTag.putInt("DataVersion", 500);
		}

		return compoundTag;
	}

	private static CompoundTag updateStructure(CompoundTag compoundTag) {
		StructureTemplate structureTemplate = new StructureTemplate();
		structureTemplate.load(NbtUtils.update(DataFixers.getDataFixer(), DataFixTypes.STRUCTURE, compoundTag, compoundTag.getInt("DataVersion")));
		return structureTemplate.save(new CompoundTag());
	}
}
