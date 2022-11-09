package net.minecraft.data.structures;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.slf4j.Logger;

public class StructureUpdater implements SnbtToNbt.Filter {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public CompoundTag apply(String string, CompoundTag compoundTag) {
		return string.startsWith("data/minecraft/structures/") ? update(string, compoundTag) : compoundTag;
	}

	public static CompoundTag update(String string, CompoundTag compoundTag) {
		return updateStructure(string, patchVersion(compoundTag));
	}

	private static CompoundTag patchVersion(CompoundTag compoundTag) {
		if (!compoundTag.contains("DataVersion", 99)) {
			compoundTag.putInt("DataVersion", 500);
		}

		return compoundTag;
	}

	private static CompoundTag updateStructure(String string, CompoundTag compoundTag) {
		StructureTemplate structureTemplate = new StructureTemplate();
		int i = compoundTag.getInt("DataVersion");
		int j = 3200;
		if (i < 3200) {
			LOGGER.warn("SNBT Too old, do not forget to update: {} < {}: {}", i, 3200, string);
		}

		CompoundTag compoundTag2 = NbtUtils.update(DataFixers.getDataFixer(), DataFixTypes.STRUCTURE, compoundTag, i);
		structureTemplate.load(BuiltInRegistries.BLOCK.asLookup(), compoundTag2);
		return structureTemplate.save(new CompoundTag());
	}
}
