package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

@Environment(EnvType.CLIENT)
public class DirectoryLister implements SpriteSource {
	public static final Codec<DirectoryLister> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.STRING.fieldOf("source").forGetter(directoryLister -> directoryLister.sourcePath),
					Codec.STRING.fieldOf("prefix").forGetter(directoryLister -> directoryLister.idPrefix)
				)
				.apply(instance, DirectoryLister::new)
	);
	private final String sourcePath;
	private final String idPrefix;

	public DirectoryLister(String string, String string2) {
		this.sourcePath = string;
		this.idPrefix = string2;
	}

	@Override
	public void run(ResourceManager resourceManager, SpriteSource.Output output) {
		FileToIdConverter fileToIdConverter = new FileToIdConverter("textures/" + this.sourcePath, ".png");
		fileToIdConverter.listMatchingResources(resourceManager).forEach((resourceLocation, resource) -> {
			ResourceLocation resourceLocation2 = fileToIdConverter.fileToId(resourceLocation).withPrefix(this.idPrefix);
			output.add(resourceLocation2, resource);
		});
	}

	@Override
	public SpriteSourceType type() {
		return SpriteSources.DIRECTORY;
	}
}
