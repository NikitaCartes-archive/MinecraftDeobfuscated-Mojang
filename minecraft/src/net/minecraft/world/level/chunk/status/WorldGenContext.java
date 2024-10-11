package net.minecraft.world.level.chunk.status;

import java.util.concurrent.Executor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public record WorldGenContext(
	ServerLevel level,
	ChunkGenerator generator,
	StructureTemplateManager structureManager,
	ThreadedLevelLightEngine lightEngine,
	Executor mainThreadExecutor,
	LevelChunk.UnsavedListener unsavedListener
) {
}
