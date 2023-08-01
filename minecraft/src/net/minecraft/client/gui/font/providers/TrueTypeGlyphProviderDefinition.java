package net.minecraft.client.gui.font.providers;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public record TrueTypeGlyphProviderDefinition(ResourceLocation location, float size, float oversample, TrueTypeGlyphProviderDefinition.Shift shift, String skip)
	implements GlyphProviderDefinition {
	private static final Codec<String> SKIP_LIST_CODEC = ExtraCodecs.withAlternative(Codec.STRING, Codec.STRING.listOf(), list -> String.join("", list));
	public static final MapCodec<TrueTypeGlyphProviderDefinition> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("file").forGetter(TrueTypeGlyphProviderDefinition::location),
					Codec.FLOAT.optionalFieldOf("size", Float.valueOf(11.0F)).forGetter(TrueTypeGlyphProviderDefinition::size),
					Codec.FLOAT.optionalFieldOf("oversample", Float.valueOf(1.0F)).forGetter(TrueTypeGlyphProviderDefinition::oversample),
					TrueTypeGlyphProviderDefinition.Shift.CODEC
						.optionalFieldOf("shift", TrueTypeGlyphProviderDefinition.Shift.NONE)
						.forGetter(TrueTypeGlyphProviderDefinition::shift),
					SKIP_LIST_CODEC.optionalFieldOf("skip", "").forGetter(TrueTypeGlyphProviderDefinition::skip)
				)
				.apply(instance, TrueTypeGlyphProviderDefinition::new)
	);

	@Override
	public GlyphProviderType type() {
		return GlyphProviderType.TTF;
	}

	@Override
	public Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack() {
		return Either.left(this::load);
	}

	private GlyphProvider load(ResourceManager resourceManager) throws IOException {
		STBTTFontinfo sTBTTFontinfo = null;
		ByteBuffer byteBuffer = null;

		try {
			InputStream inputStream = resourceManager.open(this.location.withPrefix("font/"));

			TrueTypeGlyphProvider var5;
			try {
				sTBTTFontinfo = STBTTFontinfo.malloc();
				byteBuffer = TextureUtil.readResource(inputStream);
				byteBuffer.flip();
				if (!STBTruetype.stbtt_InitFont(sTBTTFontinfo, byteBuffer)) {
					throw new IOException("Invalid ttf");
				}

				var5 = new TrueTypeGlyphProvider(byteBuffer, sTBTTFontinfo, this.size, this.oversample, this.shift.x, this.shift.y, this.skip);
			} catch (Throwable var8) {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Throwable var7) {
						var8.addSuppressed(var7);
					}
				}

				throw var8;
			}

			if (inputStream != null) {
				inputStream.close();
			}

			return var5;
		} catch (Exception var9) {
			if (sTBTTFontinfo != null) {
				sTBTTFontinfo.free();
			}

			MemoryUtil.memFree(byteBuffer);
			throw var9;
		}
	}

	@Environment(EnvType.CLIENT)
	public static record Shift(float x, float y) {
		public static final TrueTypeGlyphProviderDefinition.Shift NONE = new TrueTypeGlyphProviderDefinition.Shift(0.0F, 0.0F);
		public static final Codec<TrueTypeGlyphProviderDefinition.Shift> CODEC = Codec.FLOAT
			.listOf()
			.comapFlatMap(
				list -> Util.fixedSize(list, 2).map(listx -> new TrueTypeGlyphProviderDefinition.Shift((Float)listx.get(0), (Float)listx.get(1))),
				shift -> List.of(shift.x, shift.y)
			);
	}
}
