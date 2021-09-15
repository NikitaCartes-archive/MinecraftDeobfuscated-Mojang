package net.minecraft.util.profiling.jfr.event.worldgen;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.EventType;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

@Name("minecraft.ChunkGeneration")
@Label("Chunk generation duration")
@Category({"Minecraft", "World Generation"})
@StackTrace(false)
@DontObfuscate
public class ChunkGenerationEvent extends Event {
	public static final String EVENT_NAME = "minecraft.ChunkGeneration";
	public static final EventType TYPE = EventType.getEventType(ChunkGenerationEvent.class);
	@Name("worldPosX")
	@Label("First block x world position")
	public final int worldPosX;
	@Name("worldPosZ")
	@Label("First block z world position")
	public final int worldPosZ;
	@Name("chunkPosX")
	@Label("Chunk x position")
	public final int chunkPosX;
	@Name("chunkPosZ")
	@Label("Chunk z position")
	public final int chunkPosZ;
	@Name("status")
	public final String targetStatus;
	@Name("level")
	public final String level;
	@Name("success")
	public boolean success;

	public ChunkGenerationEvent(ChunkPos chunkPos, ResourceKey<Level> resourceKey, String string) {
		this.targetStatus = string;
		this.level = resourceKey.toString();
		this.chunkPosX = chunkPos.x;
		this.chunkPosZ = chunkPos.z;
		this.worldPosX = chunkPos.getMinBlockX();
		this.worldPosZ = chunkPos.getMinBlockZ();
	}

	public static class Fields {
		public static final String WORLD_POS_X = "worldPosX";
		public static final String WORLD_POS_Z = "worldPosZ";
		public static final String CHUNK_POS_X = "chunkPosX";
		public static final String CHUNK_POS_Z = "chunkPosZ";
		public static final String STATUS = "status";
		public static final String SUCCESS = "success";
		public static final String LEVEL = "level";

		private Fields() {
		}
	}
}
