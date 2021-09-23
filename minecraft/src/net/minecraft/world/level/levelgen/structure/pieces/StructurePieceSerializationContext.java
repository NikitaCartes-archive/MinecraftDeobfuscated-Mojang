package net.minecraft.world.level.levelgen.structure.pieces;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public record StructurePieceSerializationContext() {
	private final ResourceManager resourceManager;
	private final RegistryAccess registryAccess;
	private final StructureManager structureManager;

	public StructurePieceSerializationContext(ResourceManager resourceManager, RegistryAccess registryAccess, StructureManager structureManager) {
		this.resourceManager = resourceManager;
		this.registryAccess = registryAccess;
		this.structureManager = structureManager;
	}

	public static StructurePieceSerializationContext fromLevel(ServerLevel serverLevel) {
		MinecraftServer minecraftServer = serverLevel.getServer();
		return new StructurePieceSerializationContext(minecraftServer.getResourceManager(), minecraftServer.registryAccess(), minecraftServer.getStructureManager());
	}
}
