package net.minecraft.client.renderer.texture.atlas;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SpriteSourceList {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final FileToIdConverter ATLAS_INFO_CONVERTER = new FileToIdConverter("atlases", ".json");
	private final List<SpriteSource> sources;

	private SpriteSourceList(List<SpriteSource> list) {
		this.sources = list;
	}

	public List<Function<SpriteResourceLoader, SpriteContents>> list(ResourceManager resourceManager) {
		final Map<ResourceLocation, SpriteSource.SpriteSupplier> map = new HashMap();
		SpriteSource.Output output = new SpriteSource.Output() {
			@Override
			public void add(ResourceLocation resourceLocation, SpriteSource.SpriteSupplier spriteSupplier) {
				SpriteSource.SpriteSupplier spriteSupplier2 = (SpriteSource.SpriteSupplier)map.put(resourceLocation, spriteSupplier);
				if (spriteSupplier2 != null) {
					spriteSupplier2.discard();
				}
			}

			@Override
			public void removeAll(Predicate<ResourceLocation> predicate) {
				Iterator<Entry<ResourceLocation, SpriteSource.SpriteSupplier>> iterator = map.entrySet().iterator();

				while (iterator.hasNext()) {
					Entry<ResourceLocation, SpriteSource.SpriteSupplier> entry = (Entry<ResourceLocation, SpriteSource.SpriteSupplier>)iterator.next();
					if (predicate.test((ResourceLocation)entry.getKey())) {
						((SpriteSource.SpriteSupplier)entry.getValue()).discard();
						iterator.remove();
					}
				}
			}
		};
		this.sources.forEach(spriteSource -> spriteSource.run(resourceManager, output));
		Builder<Function<SpriteResourceLoader, SpriteContents>> builder = ImmutableList.builder();
		builder.add(spriteResourceLoader -> MissingTextureAtlasSprite.create());
		builder.addAll(map.values());
		return builder.build();
	}

	public static SpriteSourceList load(ResourceManager resourceManager, ResourceLocation resourceLocation) {
		ResourceLocation resourceLocation2 = ATLAS_INFO_CONVERTER.idToFile(resourceLocation);
		List<SpriteSource> list = new ArrayList();

		for (Resource resource : resourceManager.getResourceStack(resourceLocation2)) {
			try {
				BufferedReader bufferedReader = resource.openAsReader();

				try {
					Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, JsonParser.parseReader(bufferedReader));
					list.addAll((Collection)SpriteSources.FILE_CODEC.parse(dynamic).getOrThrow());
				} catch (Throwable var10) {
					if (bufferedReader != null) {
						try {
							bufferedReader.close();
						} catch (Throwable var9) {
							var10.addSuppressed(var9);
						}
					}

					throw var10;
				}

				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (Exception var11) {
				LOGGER.error("Failed to parse atlas definition {} in pack {}", resourceLocation2, resource.sourcePackId(), var11);
			}
		}

		return new SpriteSourceList(list);
	}
}
