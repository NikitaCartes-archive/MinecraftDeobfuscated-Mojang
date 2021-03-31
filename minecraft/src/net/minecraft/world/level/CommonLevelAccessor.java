package net.minecraft.world.level;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CommonLevelAccessor extends EntityGetter, LevelReader, LevelSimulatedRW {
	@Override
	default <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos blockPos, BlockEntityType<T> blockEntityType) {
		return LevelReader.super.getBlockEntity(blockPos, blockEntityType);
	}

	@Override
	default Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aABB, Predicate<Entity> predicate) {
		return EntityGetter.super.getEntityCollisions(entity, aABB, predicate);
	}

	@Override
	default boolean isUnobstructed(@Nullable Entity entity, VoxelShape voxelShape) {
		return EntityGetter.super.isUnobstructed(entity, voxelShape);
	}

	@Override
	default BlockPos getHeightmapPos(Heightmap.Types types, BlockPos blockPos) {
		return LevelReader.super.getHeightmapPos(types, blockPos);
	}

	RegistryAccess registryAccess();

	default Optional<ResourceKey<Biome>> getBiomeName(BlockPos blockPos) {
		return this.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getResourceKey(this.getBiome(blockPos));
	}
}
