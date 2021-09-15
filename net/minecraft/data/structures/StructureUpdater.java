/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.structures;

import net.minecraft.data.structures.SnbtToNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructureUpdater
implements SnbtToNbt.Filter {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public CompoundTag apply(String string, CompoundTag compoundTag) {
        if (string.startsWith("data/minecraft/structures/")) {
            return StructureUpdater.update(string, compoundTag);
        }
        return compoundTag;
    }

    public static CompoundTag update(String string, CompoundTag compoundTag) {
        return StructureUpdater.updateStructure(string, StructureUpdater.patchVersion(compoundTag));
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
        int j = 2830;
        if (i < 2830) {
            LOGGER.warn("SNBT Too old, do not forget to update: {} < {}: {}", (Object)i, (Object)2830, (Object)string);
        }
        CompoundTag compoundTag2 = NbtUtils.update(DataFixers.getDataFixer(), DataFixTypes.STRUCTURE, compoundTag, i);
        structureTemplate.load(compoundTag2);
        return structureTemplate.save(new CompoundTag());
    }
}

