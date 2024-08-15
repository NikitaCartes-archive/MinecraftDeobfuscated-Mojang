package net.minecraft.data.structures;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.slf4j.Logger;

public class StructureUpdater implements SnbtToNbt.Filter {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String PREFIX = PackType.SERVER_DATA.getDirectory() + "/minecraft/structure/";

	@Override
	public CompoundTag apply(String string, CompoundTag compoundTag) {
		return string.startsWith(PREFIX) ? update(string, compoundTag) : compoundTag;
	}

	public static CompoundTag update(String string, CompoundTag compoundTag) {
		StructureTemplate structureTemplate = new StructureTemplate();
		int i = NbtUtils.getDataVersion(compoundTag, 500);
		int j = 4053;
		if (i < 4053) {
			LOGGER.warn("SNBT Too old, do not forget to update: {} < {}: {}", i, 4053, string);
		}

		CompoundTag compoundTag2 = DataFixTypes.STRUCTURE.updateToCurrentVersion(DataFixers.getDataFixer(), compoundTag, i);
		structureTemplate.load(BuiltInRegistries.BLOCK.asLookup(), compoundTag2);
		return structureTemplate.save(new CompoundTag());
	}
}
