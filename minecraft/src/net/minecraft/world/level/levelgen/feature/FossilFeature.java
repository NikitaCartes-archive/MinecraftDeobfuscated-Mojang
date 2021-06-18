package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.lang3.mutable.MutableInt;

public class FossilFeature extends Feature<FossilFeatureConfiguration> {
	public FossilFeature(Codec<FossilFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<FossilFeatureConfiguration> featurePlaceContext) {
		Random random = featurePlaceContext.random();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		Rotation rotation = Rotation.getRandom(random);
		FossilFeatureConfiguration fossilFeatureConfiguration = featurePlaceContext.config();
		int i = random.nextInt(fossilFeatureConfiguration.fossilStructures.size());
		StructureManager structureManager = worldGenLevel.getLevel().getServer().getStructureManager();
		StructureTemplate structureTemplate = structureManager.getOrCreate((ResourceLocation)fossilFeatureConfiguration.fossilStructures.get(i));
		StructureTemplate structureTemplate2 = structureManager.getOrCreate((ResourceLocation)fossilFeatureConfiguration.overlayStructures.get(i));
		ChunkPos chunkPos = new ChunkPos(blockPos);
		BoundingBox boundingBox = new BoundingBox(
			chunkPos.getMinBlockX(),
			worldGenLevel.getMinBuildHeight(),
			chunkPos.getMinBlockZ(),
			chunkPos.getMaxBlockX(),
			worldGenLevel.getMaxBuildHeight(),
			chunkPos.getMaxBlockZ()
		);
		StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings().setRotation(rotation).setBoundingBox(boundingBox).setRandom(random);
		Vec3i vec3i = structureTemplate.getSize(rotation);
		int j = random.nextInt(16 - vec3i.getX());
		int k = random.nextInt(16 - vec3i.getZ());
		int l = worldGenLevel.getMaxBuildHeight();

		for (int m = 0; m < vec3i.getX(); m++) {
			for (int n = 0; n < vec3i.getZ(); n++) {
				l = Math.min(l, worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, blockPos.getX() + m + j, blockPos.getZ() + n + k));
			}
		}

		int m = Math.max(l - 15 - random.nextInt(10), worldGenLevel.getMinBuildHeight() + 10);
		BlockPos blockPos2 = structureTemplate.getZeroPositionWithTransform(blockPos.offset(j, 0, k).atY(m), Mirror.NONE, rotation);
		if (countEmptyCorners(worldGenLevel, structureTemplate.getBoundingBox(structurePlaceSettings, blockPos2)) > fossilFeatureConfiguration.maxEmptyCornersAllowed
			)
		 {
			return false;
		} else {
			structurePlaceSettings.clearProcessors();
			((StructureProcessorList)fossilFeatureConfiguration.fossilProcessors.get())
				.list()
				.forEach(structureProcessor -> structurePlaceSettings.addProcessor(structureProcessor));
			structureTemplate.placeInWorld(worldGenLevel, blockPos2, blockPos2, structurePlaceSettings, random, 4);
			structurePlaceSettings.clearProcessors();
			((StructureProcessorList)fossilFeatureConfiguration.overlayProcessors.get())
				.list()
				.forEach(structureProcessor -> structurePlaceSettings.addProcessor(structureProcessor));
			structureTemplate2.placeInWorld(worldGenLevel, blockPos2, blockPos2, structurePlaceSettings, random, 4);
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
