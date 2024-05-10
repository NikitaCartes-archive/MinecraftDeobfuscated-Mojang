package net.minecraft.world.level.chunk.status;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public record ChunkPyramid(ImmutableList<ChunkStep> steps) {
	public static final ChunkPyramid GENERATION_PYRAMID = new ChunkPyramid.Builder()
		.step(ChunkStatus.EMPTY, builder -> builder)
		.step(ChunkStatus.STRUCTURE_STARTS, builder -> builder.setTask(ChunkStatusTasks::generateStructureStarts))
		.step(
			ChunkStatus.STRUCTURE_REFERENCES, builder -> builder.addRequirement(ChunkStatus.STRUCTURE_STARTS, 8).setTask(ChunkStatusTasks::generateStructureReferences)
		)
		.step(ChunkStatus.BIOMES, builder -> builder.addRequirement(ChunkStatus.STRUCTURE_STARTS, 8).setTask(ChunkStatusTasks::generateBiomes))
		.step(
			ChunkStatus.NOISE,
			builder -> builder.addRequirement(ChunkStatus.STRUCTURE_STARTS, 8)
					.addRequirement(ChunkStatus.BIOMES, 1)
					.blockStateWriteRadius(0)
					.setTask(ChunkStatusTasks::generateNoise)
		)
		.step(
			ChunkStatus.SURFACE,
			builder -> builder.addRequirement(ChunkStatus.STRUCTURE_STARTS, 8)
					.addRequirement(ChunkStatus.BIOMES, 1)
					.blockStateWriteRadius(0)
					.setTask(ChunkStatusTasks::generateSurface)
		)
		.step(
			ChunkStatus.CARVERS, builder -> builder.addRequirement(ChunkStatus.STRUCTURE_STARTS, 8).blockStateWriteRadius(0).setTask(ChunkStatusTasks::generateCarvers)
		)
		.step(
			ChunkStatus.FEATURES,
			builder -> builder.addRequirement(ChunkStatus.STRUCTURE_STARTS, 8)
					.addRequirement(ChunkStatus.CARVERS, 1)
					.blockStateWriteRadius(1)
					.setTask(ChunkStatusTasks::generateFeatures)
		)
		.step(ChunkStatus.INITIALIZE_LIGHT, builder -> builder.setTask(ChunkStatusTasks::initializeLight))
		.step(ChunkStatus.LIGHT, builder -> builder.addRequirement(ChunkStatus.INITIALIZE_LIGHT, 1).setTask(ChunkStatusTasks::light))
		.step(ChunkStatus.SPAWN, builder -> builder.addRequirement(ChunkStatus.BIOMES, 1).setTask(ChunkStatusTasks::generateSpawn))
		.step(ChunkStatus.FULL, builder -> builder.setTask(ChunkStatusTasks::full))
		.build();
	public static final ChunkPyramid LOADING_PYRAMID = new ChunkPyramid.Builder()
		.step(ChunkStatus.EMPTY, builder -> builder)
		.step(ChunkStatus.STRUCTURE_STARTS, builder -> builder.setTask(ChunkStatusTasks::loadStructureStarts))
		.step(ChunkStatus.STRUCTURE_REFERENCES, builder -> builder)
		.step(ChunkStatus.BIOMES, builder -> builder)
		.step(ChunkStatus.NOISE, builder -> builder)
		.step(ChunkStatus.SURFACE, builder -> builder)
		.step(ChunkStatus.CARVERS, builder -> builder)
		.step(ChunkStatus.FEATURES, builder -> builder)
		.step(ChunkStatus.INITIALIZE_LIGHT, builder -> builder.setTask(ChunkStatusTasks::initializeLight))
		.step(ChunkStatus.LIGHT, builder -> builder.addRequirement(ChunkStatus.INITIALIZE_LIGHT, 1).setTask(ChunkStatusTasks::light))
		.step(ChunkStatus.SPAWN, builder -> builder)
		.step(ChunkStatus.FULL, builder -> builder.setTask(ChunkStatusTasks::full))
		.build();

	public ChunkStep getStepTo(ChunkStatus chunkStatus) {
		return (ChunkStep)this.steps.get(chunkStatus.getIndex());
	}

	public static class Builder {
		private final List<ChunkStep> steps = new ArrayList();

		public ChunkPyramid build() {
			return new ChunkPyramid(ImmutableList.copyOf(this.steps));
		}

		public ChunkPyramid.Builder step(ChunkStatus chunkStatus, UnaryOperator<ChunkStep.Builder> unaryOperator) {
			ChunkStep.Builder builder;
			if (this.steps.isEmpty()) {
				builder = new ChunkStep.Builder(chunkStatus);
			} else {
				builder = new ChunkStep.Builder(chunkStatus, (ChunkStep)this.steps.getLast());
			}

			this.steps.add(((ChunkStep.Builder)unaryOperator.apply(builder)).build());
			return this;
		}
	}
}
