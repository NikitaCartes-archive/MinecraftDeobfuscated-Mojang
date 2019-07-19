package net.minecraft.world.level.levelgen.structure;

import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructureFeatureIO {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final StructureFeature<?> MINESHAFT = register("Mineshaft", Feature.MINESHAFT);
	public static final StructureFeature<?> PILLAGER_OUTPOST = register("Pillager_Outpost", Feature.PILLAGER_OUTPOST);
	public static final StructureFeature<?> NETHER_FORTRESS = register("Fortress", Feature.NETHER_BRIDGE);
	public static final StructureFeature<?> STRONGHOLD = register("Stronghold", Feature.STRONGHOLD);
	public static final StructureFeature<?> JUNGLE_PYRAMID = register("Jungle_Pyramid", Feature.JUNGLE_TEMPLE);
	public static final StructureFeature<?> OCEAN_RUIN = register("Ocean_Ruin", Feature.OCEAN_RUIN);
	public static final StructureFeature<?> DESERT_PYRAMID = register("Desert_Pyramid", Feature.DESERT_PYRAMID);
	public static final StructureFeature<?> IGLOO = register("Igloo", Feature.IGLOO);
	public static final StructureFeature<?> SWAMP_HUT = register("Swamp_Hut", Feature.SWAMP_HUT);
	public static final StructureFeature<?> OCEAN_MONUMENT = register("Monument", Feature.OCEAN_MONUMENT);
	public static final StructureFeature<?> END_CITY = register("EndCity", Feature.END_CITY);
	public static final StructureFeature<?> WOODLAND_MANSION = register("Mansion", Feature.WOODLAND_MANSION);
	public static final StructureFeature<?> BURIED_TREASURE = register("Buried_Treasure", Feature.BURIED_TREASURE);
	public static final StructureFeature<?> SHIPWRECK = register("Shipwreck", Feature.SHIPWRECK);
	public static final StructureFeature<?> VILLAGE = register("Village", Feature.VILLAGE);

	private static StructureFeature<?> register(String string, StructureFeature<?> structureFeature) {
		return Registry.register(Registry.STRUCTURE_FEATURE, string.toLowerCase(Locale.ROOT), structureFeature);
	}

	public static void bootstrap() {
	}

	@Nullable
	public static StructureStart loadStaticStart(
		ChunkGenerator<?> chunkGenerator, StructureManager structureManager, BiomeSource biomeSource, CompoundTag compoundTag
	) {
		String string = compoundTag.getString("id");
		if ("INVALID".equals(string)) {
			return StructureStart.INVALID_START;
		} else {
			StructureFeature<?> structureFeature = Registry.STRUCTURE_FEATURE.get(new ResourceLocation(string.toLowerCase(Locale.ROOT)));
			if (structureFeature == null) {
				LOGGER.error("Unknown feature id: {}", string);
				return null;
			} else {
				int i = compoundTag.getInt("ChunkX");
				int j = compoundTag.getInt("ChunkZ");
				Biome biome = compoundTag.contains("biome")
					? Registry.BIOME.get(new ResourceLocation(compoundTag.getString("biome")))
					: biomeSource.getBiome(new BlockPos((i << 4) + 9, 0, (j << 4) + 9));
				BoundingBox boundingBox = compoundTag.contains("BB") ? new BoundingBox(compoundTag.getIntArray("BB")) : BoundingBox.getUnknownBox();
				ListTag listTag = compoundTag.getList("Children", 10);

				try {
					StructureStart structureStart = structureFeature.getStartFactory().create(structureFeature, i, j, biome, boundingBox, 0, chunkGenerator.getSeed());

					for (int k = 0; k < listTag.size(); k++) {
						CompoundTag compoundTag2 = listTag.getCompound(k);
						String string2 = compoundTag2.getString("id");
						StructurePieceType structurePieceType = Registry.STRUCTURE_PIECE.get(new ResourceLocation(string2.toLowerCase(Locale.ROOT)));
						if (structurePieceType == null) {
							LOGGER.error("Unknown structure piece id: {}", string2);
						} else {
							try {
								StructurePiece structurePiece = structurePieceType.load(structureManager, compoundTag2);
								structureStart.pieces.add(structurePiece);
							} catch (Exception var17) {
								LOGGER.error("Exception loading structure piece with id {}", string2, var17);
							}
						}
					}

					return structureStart;
				} catch (Exception var18) {
					LOGGER.error("Failed Start with id {}", string, var18);
					return null;
				}
			}
		}
	}
}
