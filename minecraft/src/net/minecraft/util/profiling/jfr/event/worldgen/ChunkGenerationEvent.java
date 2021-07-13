package net.minecraft.util.profiling.jfr.event.worldgen;

import javax.annotation.Nullable;
import jdk.jfr.Category;
import jdk.jfr.Label;
import jdk.jfr.Name;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.util.profiling.jfr.event.AbstractMinecraftJfrEvent;

@Name("minecraft.ChunkGeneration")
@Label("ChunkGeneration")
@Category({"Minecraft", "World Generation"})
@DontObfuscate
public class ChunkGenerationEvent extends AbstractMinecraftJfrEvent {
	public static final String NAME = "minecraft.ChunkGeneration";
	@Label("centerBlockPosX")
	public int centerBlockPosX;
	@Label("centerBlockPosZ")
	public int centerBlockPosZ;
	@Label("chunkPosX")
	public int chunkPosX;
	@Label("chunkPosZ")
	public int chunkPosZ;
	@Label("status")
	@Nullable
	public String targetStatus;
	@Label("success")
	public boolean success;
	@Label("level")
	@Nullable
	public String level;
}
