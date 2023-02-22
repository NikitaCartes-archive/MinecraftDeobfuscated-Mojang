package net.minecraft.client.renderer.texture.atlas;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.client.renderer.texture.atlas.sources.SourceFilter;
import net.minecraft.client.renderer.texture.atlas.sources.Unstitcher;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class SpriteSources {
	private static final BiMap<ResourceLocation, SpriteSourceType> TYPES = HashBiMap.create();
	public static final SpriteSourceType SINGLE_FILE = register("single", SingleFile.CODEC);
	public static final SpriteSourceType DIRECTORY = register("directory", DirectoryLister.CODEC);
	public static final SpriteSourceType FILTER = register("filter", SourceFilter.CODEC);
	public static final SpriteSourceType UNSTITCHER = register("unstitch", Unstitcher.CODEC);
	public static final SpriteSourceType PALETTED_PERMUTATIONS = register("paletted_permutations", PalettedPermutations.CODEC);
	public static Codec<SpriteSourceType> TYPE_CODEC = ResourceLocation.CODEC.flatXmap(resourceLocation -> {
		SpriteSourceType spriteSourceType = (SpriteSourceType)TYPES.get(resourceLocation);
		return spriteSourceType != null ? DataResult.success(spriteSourceType) : DataResult.error(() -> "Unknown type " + resourceLocation);
	}, spriteSourceType -> {
		ResourceLocation resourceLocation = (ResourceLocation)TYPES.inverse().get(spriteSourceType);
		return spriteSourceType != null ? DataResult.success(resourceLocation) : DataResult.error(() -> "Unknown type " + resourceLocation);
	});
	public static Codec<SpriteSource> CODEC = TYPE_CODEC.dispatch(SpriteSource::type, SpriteSourceType::codec);
	public static Codec<List<SpriteSource>> FILE_CODEC = CODEC.listOf().fieldOf("sources").codec();

	private static SpriteSourceType register(String string, Codec<? extends SpriteSource> codec) {
		SpriteSourceType spriteSourceType = new SpriteSourceType(codec);
		ResourceLocation resourceLocation = new ResourceLocation(string);
		SpriteSourceType spriteSourceType2 = (SpriteSourceType)TYPES.putIfAbsent(resourceLocation, spriteSourceType);
		if (spriteSourceType2 != null) {
			throw new IllegalStateException("Duplicate registration " + resourceLocation);
		} else {
			return spriteSourceType;
		}
	}
}
