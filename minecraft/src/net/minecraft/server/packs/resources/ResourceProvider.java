package net.minecraft.server.packs.resources;

import java.io.IOException;
import net.minecraft.resources.ResourceLocation;

public interface ResourceProvider {
	Resource getResource(ResourceLocation resourceLocation) throws IOException;
}
