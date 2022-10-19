package net.minecraft.resources;

import java.util.List;
import java.util.Map;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public class FileToIdConverter {
	private final String prefix;
	private final String extension;

	public FileToIdConverter(String string, String string2) {
		this.prefix = string;
		this.extension = string2;
	}

	public static FileToIdConverter json(String string) {
		return new FileToIdConverter(string, ".json");
	}

	public ResourceLocation idToFile(ResourceLocation resourceLocation) {
		return resourceLocation.withPath(this.prefix + "/" + resourceLocation.getPath() + this.extension);
	}

	public ResourceLocation fileToId(ResourceLocation resourceLocation) {
		String string = resourceLocation.getPath();
		return resourceLocation.withPath(string.substring(this.prefix.length() + 1, string.length() - this.extension.length()));
	}

	public Map<ResourceLocation, Resource> listMatchingResources(ResourceManager resourceManager) {
		return resourceManager.listResources(this.prefix, resourceLocation -> resourceLocation.getPath().endsWith(this.extension));
	}

	public Map<ResourceLocation, List<Resource>> listMatchingResourceStacks(ResourceManager resourceManager) {
		return resourceManager.listResourceStacks(this.prefix, resourceLocation -> resourceLocation.getPath().endsWith(this.extension));
	}
}
