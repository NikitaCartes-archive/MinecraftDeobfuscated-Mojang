package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.apache.commons.lang3.mutable.MutableInt;

public class FossilFeature extends Feature<FossilFeatureConfiguration> {
	public FossilFeature(Codec<FossilFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<FossilFeatureConfiguration> featurePlaceContext) {
		RandomSource randomSource = featurePlaceContext.random();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		Rotation rotation = Rotation.getRandom(randomSource);
		FossilFeatureConfiguration fossilFeatureConfiguration = featurePlaceContext.config();
		int i = randomSource.nextInt(fossilFeatureConfiguration.fossilStructures.size());
		StructureTemplateManager structureTemplateManager = worldGenLevel.getLevel().getServer().getStructureManager();
		StructureTemplate structureTemplate = structureTemplateManager.getOrCreate((ResourceLocation)fossilFeatureConfiguration.fossilStructures.get(i));
		StructureTemplate structureTemplate2 = structureTemplateManager.getOrCreate((ResourceLocation)fossilFeatureConfiguration.overlayStructures.get(i));
		ChunkPos chunkPos = new ChunkPos(blockPos);
		BoundingBox boundingBox = new BoundingBox(
			chunkPos.getMinBlockX() - 16,
			worldGenLevel.getMinBuildHeight(),
			chunkPos.getMinBlockZ() - 16,
			chunkPos.getMaxBlockX() + 16,
			worldGenLevel.getMaxBuildHeight(),
			chunkPos.getMaxBlockZ() + 16
		);
		StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings().setRotation(rotation).setBoundingBox(boundingBox).setRandom(randomSource);
		Vec3i vec3i = structureTemplate.getSize(rotation);
		BlockPos blockPos2 = blockPos.offset(-vec3i.getX() / 2, 0, -vec3i.getZ() / 2);
		int j = blockPos.getY();

		for (int k = 0; k < vec3i.getX(); k++) {
			for (int l = 0; l < vec3i.getZ(); l++) {
				j = Math.min(j, worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, blockPos2.getX() + k, blockPos2.getZ() + l));
			}
		}

		int k = Math.max(j - 15 - randomSource.nextInt(10), worldGenLevel.getMinBuildHeight() + 10);
		BlockPos blockPos3 = structureTemplate.getZeroPositionWithTransform(blockPos2.atY(k), Mirror.NONE, rotation);
		if (countEmptyCorners(worldGenLevel, structureTemplate.getBoundingBox(structurePlaceSettings, blockPos3)) > fossilFeatureConfiguration.maxEmptyCornersAllowed
			)
		 {
			return false;
		} else {
			structurePlaceSettings.clearProcessors();
			fossilFeatureConfiguration.fossilProcessors.value().list().forEach(structurePlaceSettings::addProcessor);
			structureTemplate.placeInWorld(worldGenLevel, blockPos3, blockPos3, structurePlaceSettings, randomSource, 4);
			structurePlaceSettings.clearProcessors();
			fossilFeatureConfiguration.overlayProcessors.value().list().forEach(structurePlaceSettings::addProcessor);
			structureTemplate2.placeInWorld(worldGenLevel, blockPos3, blockPos3, structurePlaceSettings, randomSource, 4);
			return true;
		}
	}

	private static int countEmptyCorners(WorldGenLevel worldGenLevel, BoundingBox boundingBox) {
		MutableInt mutableInt = new MutableInt(0);
		boundingBox.forAllCorners(blockPos -> {
			BlockState blockState = worldGenLevel.getBlockState(blockPos);
			if (blockState.isAir() || blockState.is(Blocks.LAVA) || blockState.is(Blocks.WATER)) {
				mutableInt.add(1);
			}
		});
		return mutableInt.getValue();
	}
}
