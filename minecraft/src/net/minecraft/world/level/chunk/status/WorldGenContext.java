package net.minecraft.world.level.chunk.status;

import net.minecraft.server.level.ChunkTaskPriorityQueueSorter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public record WorldGenContext(
	ServerLevel level,
	ChunkGenerator generator,
	StructureTemplateManager structureManager,
	ThreadedLevelLightEngine lightEngine,
	ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> mainThreadMailBox
) {
}
