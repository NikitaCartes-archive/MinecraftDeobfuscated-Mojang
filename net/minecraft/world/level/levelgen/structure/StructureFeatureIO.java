/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure;

import java.util.Locale;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class StructureFeatureIO {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final StructureFeature<?> MINESHAFT = StructureFeatureIO.register("Mineshaft", Feature.MINESHAFT);
    public static final StructureFeature<?> PILLAGER_OUTPOST = StructureFeatureIO.register("Pillager_Outpost", Feature.PILLAGER_OUTPOST);
    public static final StructureFeature<?> NETHER_FORTRESS = StructureFeatureIO.register("Fortress", Feature.NETHER_BRIDGE);
    public static final StructureFeature<?> STRONGHOLD = StructureFeatureIO.register("Stronghold", Feature.STRONGHOLD);
    public static final StructureFeature<?> JUNGLE_PYRAMID = StructureFeatureIO.register("Jungle_Pyramid", Feature.JUNGLE_TEMPLE);
    public static final StructureFeature<?> OCEAN_RUIN = StructureFeatureIO.register("Ocean_Ruin", Feature.OCEAN_RUIN);
    public static final StructureFeature<?> DESERT_PYRAMID = StructureFeatureIO.register("Desert_Pyramid", Feature.DESERT_PYRAMID);
    public static final StructureFeature<?> IGLOO = StructureFeatureIO.register("Igloo", Feature.IGLOO);
    public static final StructureFeature<?> SWAMP_HUT = StructureFeatureIO.register("Swamp_Hut", Feature.SWAMP_HUT);
    public static final StructureFeature<?> OCEAN_MONUMENT = StructureFeatureIO.register("Monument", Feature.OCEAN_MONUMENT);
    public static final StructureFeature<?> END_CITY = StructureFeatureIO.register("EndCity", Feature.END_CITY);
    public static final StructureFeature<?> WOODLAND_MANSION = StructureFeatureIO.register("Mansion", Feature.WOODLAND_MANSION);
    public static final StructureFeature<?> BURIED_TREASURE = StructureFeatureIO.register("Buried_Treasure", Feature.BURIED_TREASURE);
    public static final StructureFeature<?> SHIPWRECK = StructureFeatureIO.register("Shipwreck", Feature.SHIPWRECK);
    public static final StructureFeature<?> VILLAGE = StructureFeatureIO.register("Village", Feature.VILLAGE);

    private static StructureFeature<?> register(String string, StructureFeature<?> structureFeature) {
        return Registry.register(Registry.STRUCTURE_FEATURE, string.toLowerCase(Locale.ROOT), structureFeature);
    }

    public static void bootstrap() {
    }

    @Nullable
    public static StructureStart loadStaticStart(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, CompoundTag compoundTag) {
        String string = compoundTag.getString("id");
        if ("INVALID".equals(string)) {
            return StructureStart.INVALID_START;
        }
        StructureFeature<?> structureFeature = Registry.STRUCTURE_FEATURE.get(new ResourceLocation(string.toLowerCase(Locale.ROOT)));
        if (structureFeature == null) {
            LOGGER.error("Unknown feature id: {}", (Object)string);
            return null;
        }
        int i = compoundTag.getInt("ChunkX");
        int j = compoundTag.getInt("ChunkZ");
        int k = compoundTag.getInt("references");
        BoundingBox boundingBox = compoundTag.contains("BB") ? new BoundingBox(compoundTag.getIntArray("BB")) : BoundingBox.getUnknownBox();
        ListTag listTag = compoundTag.getList("Children", 10);
        try {
            StructureStart structureStart = structureFeature.getStartFactory().create(structureFeature, i, j, boundingBox, k, chunkGenerator.getSeed());
            for (int l = 0; l < listTag.size(); ++l) {
                CompoundTag compoundTag2 = listTag.getCompound(l);
                String string2 = compoundTag2.getString("id");
                StructurePieceType structurePieceType = Registry.STRUCTURE_PIECE.get(new ResourceLocation(string2.toLowerCase(Locale.ROOT)));
                if (structurePieceType == null) {
                    LOGGER.error("Unknown structure piece id: {}", (Object)string2);
                    continue;
                }
                try {
                    StructurePiece structurePiece = structurePieceType.load(structureManager, compoundTag2);
                    structureStart.pieces.add(structurePiece);
                    continue;
                } catch (Exception exception) {
                    LOGGER.error("Exception loading structure piece with id {}", (Object)string2, (Object)exception);
                }
            }
            return structureStart;
        } catch (Exception exception2) {
            LOGGER.error("Failed Start with id {}", (Object)string, (Object)exception2);
            return null;
        }
    }
}

