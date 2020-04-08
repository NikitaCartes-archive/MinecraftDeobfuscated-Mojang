package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MonsterRoomFeature extends Feature<NoneFeatureConfiguration> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final EntityType<?>[] MOBS = new EntityType[]{EntityType.SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE, EntityType.SPIDER};
	private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

	public MonsterRoomFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		int i = 3;
		int j = random.nextInt(2) + 2;
		int k = -j - 1;
		int l = j + 1;
		int m = -1;
		int n = 4;
		int o = random.nextInt(2) + 2;
		int p = -o - 1;
		int q = o + 1;
		int r = 0;

		for (int s = k; s <= l; s++) {
			for (int t = -1; t <= 4; t++) {
				for (int u = p; u <= q; u++) {
					BlockPos blockPos2 = blockPos.offset(s, t, u);
					Material material = levelAccessor.getBlockState(blockPos2).getMaterial();
					boolean bl = material.isSolid();
					if (t == -1 && !bl) {
						return false;
					}

					if (t == 4 && !bl) {
						return false;
					}

					if ((s == k || s == l || u == p || u == q) && t == 0 && levelAccessor.isEmptyBlock(blockPos2) && levelAccessor.isEmptyBlock(blockPos2.above())) {
						r++;
					}
				}
			}
		}

		if (r >= 1 && r <= 5) {
			for (int s = k; s <= l; s++) {
				for (int t = 3; t >= -1; t--) {
					for (int u = p; u <= q; u++) {
						BlockPos blockPos2x = blockPos.offset(s, t, u);
						if (s != k && t != -1 && u != p && s != l && t != 4 && u != q) {
							if (levelAccessor.getBlockState(blockPos2x).getBlock() != Blocks.CHEST) {
								levelAccessor.setBlock(blockPos2x, AIR, 2);
							}
						} else if (blockPos2x.getY() >= 0 && !levelAccessor.getBlockState(blockPos2x.below()).getMaterial().isSolid()) {
							levelAccessor.setBlock(blockPos2x, AIR, 2);
						} else if (levelAccessor.getBlockState(blockPos2x).getMaterial().isSolid() && levelAccessor.getBlockState(blockPos2x).getBlock() != Blocks.CHEST) {
							if (t == -1 && random.nextInt(4) != 0) {
								levelAccessor.setBlock(blockPos2x, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
							} else {
								levelAccessor.setBlock(blockPos2x, Blocks.COBBLESTONE.defaultBlockState(), 2);
							}
						}
					}
				}
			}

			for (int s = 0; s < 2; s++) {
				for (int t = 0; t < 3; t++) {
					int ux = blockPos.getX() + random.nextInt(j * 2 + 1) - j;
					int v = blockPos.getY();
					int w = blockPos.getZ() + random.nextInt(o * 2 + 1) - o;
					BlockPos blockPos3 = new BlockPos(ux, v, w);
					if (levelAccessor.isEmptyBlock(blockPos3)) {
						int x = 0;

						for (Direction direction : Direction.Plane.HORIZONTAL) {
							if (levelAccessor.getBlockState(blockPos3.relative(direction)).getMaterial().isSolid()) {
								x++;
							}
						}

						if (x == 1) {
							levelAccessor.setBlock(blockPos3, StructurePiece.reorient(levelAccessor, blockPos3, Blocks.CHEST.defaultBlockState()), 2);
							RandomizableContainerBlockEntity.setLootTable(levelAccessor, random, blockPos3, BuiltInLootTables.SIMPLE_DUNGEON);
							break;
						}
					}
				}
			}

			levelAccessor.setBlock(blockPos, Blocks.SPAWNER.defaultBlockState(), 2);
			BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
			if (blockEntity instanceof SpawnerBlockEntity) {
				((SpawnerBlockEntity)blockEntity).getSpawner().setEntityId(this.randomEntityId(random));
			} else {
				LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", blockPos.getX(), blockPos.getY(), blockPos.getZ());
			}

			return true;
		} else {
			return false;
		}
	}

	private EntityType<?> randomEntityId(Random random) {
		return Util.getRandom(MOBS, random);
	}
}
