package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SingleFile implements SpriteSource {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Codec<SingleFile> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("resource").forGetter(singleFile -> singleFile.resourceId),
					ResourceLocation.CODEC.optionalFieldOf("sprite").forGetter(singleFile -> singleFile.spriteId)
				)
				.apply(instance, SingleFile::new)
	);
	private final FileToIdConverter TEXTURE_ID_CONVERTER = new FileToIdConverter("textures", ".png");
	private final ResourceLocation resourceId;
	private final Optional<ResourceLocation> spriteId;

	public SingleFile(ResourceLocation resourceLocation, Optional<ResourceLocation> optional) {
		this.resourceId = resourceLocation;
		this.spriteId = optional;
	}

	@Override
	public void run(ResourceManager resourceManager, SpriteSource.Output output) {
		ResourceLocation resourceLocation = this.TEXTURE_ID_CONVERTER.idToFile(this.resourceId);
		Optional<Resource> optional = resourceManager.getResource(resourceLocation);
		if (optional.isPresent()) {
			output.add((ResourceLocation)this.spriteId.orElse(this.resourceId), (Resource)optional.get());
		} else {
			LOGGER.warn("Missing sprite: {}", resourceLocation);
		}
	}

	@Override
	public SpriteSourceType type() {
		return SpriteSources.SINGLE_FILE;
	}
}
