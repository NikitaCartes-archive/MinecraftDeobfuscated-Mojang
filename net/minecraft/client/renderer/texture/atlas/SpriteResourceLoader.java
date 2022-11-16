/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture.atlas;

import com.google.common.collect.ImmutableList;
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
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class SpriteResourceLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter ATLAS_INFO_CONVERTER = new FileToIdConverter("atlases", ".json");
    private final List<SpriteSource> sources;

    private SpriteResourceLoader(List<SpriteSource> list) {
        this.sources = list;
    }

    public List<Supplier<SpriteContents>> list(ResourceManager resourceManager) {
        final HashMap map = new HashMap();
        SpriteSource.Output output = new SpriteSource.Output(){

            @Override
            public void add(ResourceLocation resourceLocation, SpriteSource.SpriteSupplier spriteSupplier) {
                SpriteSource.SpriteSupplier spriteSupplier2 = map.put(resourceLocation, spriteSupplier);
                if (spriteSupplier2 != null) {
                    spriteSupplier2.discard();
                }
            }

            @Override
            public void removeAll(Predicate<ResourceLocation> predicate) {
                Iterator iterator = map.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = iterator.next();
                    if (!predicate.test((ResourceLocation)entry.getKey())) continue;
                    ((SpriteSource.SpriteSupplier)entry.getValue()).discard();
                    iterator.remove();
                }
            }
        };
        this.sources.forEach(spriteSource -> spriteSource.run(resourceManager, output));
        ImmutableList.Builder builder = ImmutableList.builder();
        builder.add(MissingTextureAtlasSprite::create);
        builder.addAll((Iterable)map.values());
        return builder.build();
    }

    public static SpriteResourceLoader load(ResourceManager resourceManager, ResourceLocation resourceLocation) {
        ResourceLocation resourceLocation2 = ATLAS_INFO_CONVERTER.idToFile(resourceLocation);
        ArrayList<SpriteSource> list = new ArrayList<SpriteSource>();
        for (Resource resource : resourceManager.getResourceStack(resourceLocation2)) {
            try {
                BufferedReader bufferedReader = resource.openAsReader();
                try {
                    Dynamic<JsonElement> dynamic = new Dynamic<JsonElement>(JsonOps.INSTANCE, JsonParser.parseReader(bufferedReader));
                    list.addAll((Collection)SpriteSources.FILE_CODEC.parse(dynamic).getOrThrow(false, LOGGER::error));
                } finally {
                    if (bufferedReader == null) continue;
                    bufferedReader.close();
                }
            } catch (Exception exception) {
                LOGGER.warn("Failed to parse atlas definition {} in pack {}", resourceLocation2, resource.sourcePackId(), exception);
            }
        }
        return new SpriteResourceLoader(list);
    }
}

