package net.minecraft.util.profiling.jfr.event;

import jdk.jfr.EventType;
import jdk.jfr.Label;
import jdk.jfr.Name;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;

@Name("minecraft.ChunkRegionWrite")
@Label("Region File Write")
@DontObfuscate
public class ChunkRegionWriteEvent extends ChunkRegionIoEvent {
	public static final String EVENT_NAME = "minecraft.ChunkRegionWrite";
	public static final EventType TYPE = EventType.getEventType(ChunkRegionWriteEvent.class);

	public ChunkRegionWriteEvent(RegionStorageInfo regionStorageInfo, ChunkPos chunkPos, RegionFileVersion regionFileVersion, int i) {
		super(regionStorageInfo, chunkPos, regionFileVersion, i);
	}
}
