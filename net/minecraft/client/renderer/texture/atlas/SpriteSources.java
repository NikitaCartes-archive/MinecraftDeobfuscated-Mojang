/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.texture.atlas;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.client.renderer.texture.atlas.sources.SourceFilter;
import net.minecraft.client.renderer.texture.atlas.sources.Unstitcher;
import net.minecraft.resources.ResourceLocation;

@Environment(value=EnvType.CLIENT)
public class SpriteSources {
    private static final BiMap<ResourceLocation, SpriteSourceType> TYPES = HashBiMap.create();
    public static final SpriteSourceType SINGLE_FILE = SpriteSources.register("single", SingleFile.CODEC);
    public static final SpriteSourceType DIRECTORY = SpriteSources.register("directory", DirectoryLister.CODEC);
    public static final SpriteSourceType FILTER = SpriteSources.register("filter", SourceFilter.CODEC);
    public static final SpriteSourceType UNSTITCHER = SpriteSources.register("unstitch", Unstitcher.CODEC);
    public static final SpriteSourceType PALETTED_PERMUTATIONS = SpriteSources.register("paletted_permutations", PalettedPermutations.CODEC);
    public static Codec<SpriteSourceType> TYPE_CODEC = ResourceLocation.CODEC.flatXmap(resourceLocation -> {
        SpriteSourceType spriteSourceType = (SpriteSourceType)TYPES.get(resourceLocation);
        return spriteSourceType != null ? DataResult.success(spriteSourceType) : DataResult.error("Unknown type " + resourceLocation);
    }, spriteSourceType -> {
        ResourceLocation resourceLocation = (ResourceLocation)TYPES.inverse().get(spriteSourceType);
        return spriteSourceType != null ? DataResult.success(resourceLocation) : DataResult.error("Unknown type " + resourceLocation);
    });
    public static Codec<SpriteSource> CODEC = TYPE_CODEC.dispatch(SpriteSource::type, SpriteSourceType::codec);
    public static Codec<List<SpriteSource>> FILE_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)CODEC.listOf().fieldOf("sources")).forGetter(list -> list)).apply((Applicative<List, ?>)instance, list -> list));

    private static SpriteSourceType register(String string, Codec<? extends SpriteSource> codec) {
        ResourceLocation resourceLocation = new ResourceLocation(string);
        SpriteSourceType spriteSourceType = new SpriteSourceType(codec);
        SpriteSourceType spriteSourceType2 = TYPES.putIfAbsent(resourceLocation, spriteSourceType);
        if (spriteSourceType2 != null) {
            throw new IllegalStateException("Duplicate registration " + resourceLocation);
        }
        return spriteSourceType;
    }
}

