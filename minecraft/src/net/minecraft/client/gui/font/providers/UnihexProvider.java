package net.minecraft.client.gui.font.providers;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FastBufferedInputStream;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class UnihexProvider implements GlyphProvider {
	static final Logger LOGGER = LogUtils.getLogger();
	private static final int GLYPH_HEIGHT = 16;
	private static final int DIGITS_PER_BYTE = 2;
	private static final int DIGITS_FOR_WIDTH_8 = 32;
	private static final int DIGITS_FOR_WIDTH_16 = 64;
	private static final int DIGITS_FOR_WIDTH_24 = 96;
	private static final int DIGITS_FOR_WIDTH_32 = 128;
	private final CodepointMap<UnihexProvider.Glyph> glyphs;

	UnihexProvider(CodepointMap<UnihexProvider.Glyph> codepointMap) {
		this.glyphs = codepointMap;
	}

	@Nullable
	@Override
	public GlyphInfo getGlyph(int i) {
		return this.glyphs.get(i);
	}

	@Override
	public IntSet getSupportedGlyphs() {
		return this.glyphs.keySet();
	}

	@VisibleForTesting
	static void unpackBitsToBytes(IntBuffer intBuffer, int i, int j, int k) {
		int l = 32 - j - 1;
		int m = 32 - k - 1;

		for (int n = l; n >= m; n--) {
			if (n < 32 && n >= 0) {
				boolean bl = (i >> n & 1) != 0;
				intBuffer.put(bl ? -1 : 0);
			} else {
				intBuffer.put(0);
			}
		}
	}

	static void unpackBitsToBytes(IntBuffer intBuffer, UnihexProvider.LineData lineData, int i, int j) {
		for (int k = 0; k < 16; k++) {
			int l = lineData.line(k);
			unpackBitsToBytes(intBuffer, l, i, j);
		}
	}

	@VisibleForTesting
	static void readFromStream(InputStream inputStream, UnihexProvider.ReaderOutput readerOutput) throws IOException {
		int i = 0;
		ByteList byteList = new ByteArrayList(128);

		while (true) {
			boolean bl = copyUntil(inputStream, byteList, 58);
			int j = byteList.size();
			if (j == 0 && !bl) {
				return;
			}

			if (!bl || j != 4 && j != 5 && j != 6) {
				throw new IllegalArgumentException("Invalid entry at line " + i + ": expected 4, 5 or 6 hex digits followed by a colon");
			}

			int k = 0;

			for (int l = 0; l < j; l++) {
				k = k << 4 | decodeHex(i, byteList.getByte(l));
			}

			byteList.clear();
			copyUntil(inputStream, byteList, 10);
			int l = byteList.size();

			UnihexProvider.LineData lineData = switch (l) {
				case 32 -> UnihexProvider.ByteContents.read(i, byteList);
				case 64 -> UnihexProvider.ShortContents.read(i, byteList);
				case 96 -> UnihexProvider.IntContents.read24(i, byteList);
				case 128 -> UnihexProvider.IntContents.read32(i, byteList);
				default -> throw new IllegalArgumentException(
				"Invalid entry at line " + i + ": expected hex number describing (8,16,24,32) x 16 bitmap, followed by a new line"
			);
			};
			readerOutput.accept(k, lineData);
			i++;
			byteList.clear();
		}
	}

	static int decodeHex(int i, ByteList byteList, int j) {
		return decodeHex(i, byteList.getByte(j));
	}

	private static int decodeHex(int i, byte b) {
		return switch (b) {
			case 48 -> 0;
			case 49 -> 1;
			case 50 -> 2;
			case 51 -> 3;
			case 52 -> 4;
			case 53 -> 5;
			case 54 -> 6;
			case 55 -> 7;
			case 56 -> 8;
			case 57 -> 9;
			default -> throw new IllegalArgumentException("Invalid entry at line " + i + ": expected hex digit, got " + (char)b);
			case 65 -> 10;
			case 66 -> 11;
			case 67 -> 12;
			case 68 -> 13;
			case 69 -> 14;
			case 70 -> 15;
		};
	}

	private static boolean copyUntil(InputStream inputStream, ByteList byteList, int i) throws IOException {
		while (true) {
			int j = inputStream.read();
			if (j == -1) {
				return false;
			}

			if (j == i) {
				return true;
			}

			byteList.add((byte)j);
		}
	}

	@Environment(EnvType.CLIENT)
	static record ByteContents(byte[] contents) implements UnihexProvider.LineData {
		@Override
		public int line(int i) {
			return this.contents[i] << 24;
		}

		static UnihexProvider.LineData read(int i, ByteList byteList) {
			byte[] bs = new byte[16];
			int j = 0;

			for (int k = 0; k < 16; k++) {
				int l = UnihexProvider.decodeHex(i, byteList, j++);
				int m = UnihexProvider.decodeHex(i, byteList, j++);
				byte b = (byte)(l << 4 | m);
				bs[k] = b;
			}

			return new UnihexProvider.ByteContents(bs);
		}

		@Override
		public int bitWidth() {
			return 8;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Definition implements GlyphProviderDefinition {
		public static final MapCodec<UnihexProvider.Definition> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						ResourceLocation.CODEC.fieldOf("hex_file").forGetter(definition -> definition.hexFile),
						UnihexProvider.OverrideRange.CODEC.listOf().fieldOf("size_overrides").forGetter(definition -> definition.sizeOverrides)
					)
					.apply(instance, UnihexProvider.Definition::new)
		);
		private final ResourceLocation hexFile;
		private final List<UnihexProvider.OverrideRange> sizeOverrides;

		private Definition(ResourceLocation resourceLocation, List<UnihexProvider.OverrideRange> list) {
			this.hexFile = resourceLocation;
			this.sizeOverrides = list;
		}

		@Override
		public GlyphProviderType type() {
			return GlyphProviderType.UNIHEX;
		}

		@Override
		public Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack() {
			return Either.left(this::load);
		}

		private GlyphProvider load(ResourceManager resourceManager) throws IOException {
			InputStream inputStream = resourceManager.open(this.hexFile);

			UnihexProvider var3;
			try {
				var3 = this.loadData(inputStream);
			} catch (Throwable var6) {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Throwable var5) {
						var6.addSuppressed(var5);
					}
				}

				throw var6;
			}

			if (inputStream != null) {
				inputStream.close();
			}

			return var3;
		}

		private UnihexProvider loadData(InputStream inputStream) throws IOException {
			CodepointMap<UnihexProvider.LineData> codepointMap = new CodepointMap<>(UnihexProvider.LineData[]::new, UnihexProvider.LineData[][]::new);
			UnihexProvider.ReaderOutput readerOutput = codepointMap::put;
			ZipInputStream zipInputStream = new ZipInputStream(inputStream);

			UnihexProvider var17;
			try {
				ZipEntry zipEntry;
				while ((zipEntry = zipInputStream.getNextEntry()) != null) {
					String string = zipEntry.getName();
					if (string.endsWith(".hex")) {
						UnihexProvider.LOGGER.info("Found {}, loading", string);
						UnihexProvider.readFromStream(new FastBufferedInputStream(zipInputStream), readerOutput);
					}
				}

				CodepointMap<UnihexProvider.Glyph> codepointMap2 = new CodepointMap<>(UnihexProvider.Glyph[]::new, UnihexProvider.Glyph[][]::new);

				for (UnihexProvider.OverrideRange overrideRange : this.sizeOverrides) {
					int i = overrideRange.from;
					int j = overrideRange.to;
					UnihexProvider.Dimensions dimensions = overrideRange.dimensions;

					for (int k = i; k <= j; k++) {
						UnihexProvider.LineData lineData = codepointMap.remove(k);
						if (lineData != null) {
							codepointMap2.put(k, new UnihexProvider.Glyph(lineData, dimensions.left, dimensions.right));
						}
					}
				}

				codepointMap.forEach((ix, lineDatax) -> {
					int jx = lineDatax.calculateWidth();
					int kx = UnihexProvider.Dimensions.left(jx);
					int l = UnihexProvider.Dimensions.right(jx);
					codepointMap2.put(ix, new UnihexProvider.Glyph(lineDatax, kx, l));
				});
				var17 = new UnihexProvider(codepointMap2);
			} catch (Throwable var15) {
				try {
					zipInputStream.close();
				} catch (Throwable var14) {
					var15.addSuppressed(var14);
				}

				throw var15;
			}

			zipInputStream.close();
			return var17;
		}
	}

	@Environment(EnvType.CLIENT)
	public static record Dimensions(int left, int right) {
		public static final MapCodec<UnihexProvider.Dimensions> MAP_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						Codec.INT.fieldOf("left").forGetter(UnihexProvider.Dimensions::left), Codec.INT.fieldOf("right").forGetter(UnihexProvider.Dimensions::right)
					)
					.apply(instance, UnihexProvider.Dimensions::new)
		);
		public static final Codec<UnihexProvider.Dimensions> CODEC = MAP_CODEC.codec();

		public int pack() {
			return pack(this.left, this.right);
		}

		public static int pack(int i, int j) {
			return (i & 0xFF) << 8 | j & 0xFF;
		}

		public static int left(int i) {
			return (byte)(i >> 8);
		}

		public static int right(int i) {
			return (byte)i;
		}
	}

	@Environment(EnvType.CLIENT)
	static record Glyph(UnihexProvider.LineData contents, int left, int right) implements GlyphInfo {

		public int width() {
			return this.right - this.left + 1;
		}

		@Override
		public float getAdvance() {
			return (float)(this.width() / 2 + 1);
		}

		@Override
		public float getShadowOffset() {
			return 0.5F;
		}

		@Override
		public float getBoldOffset() {
			return 0.5F;
		}

		@Override
		public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
			return (BakedGlyph)function.apply(new SheetGlyphInfo() {
				@Override
				public float getOversample() {
					return 2.0F;
				}

				@Override
				public int getPixelWidth() {
					return Glyph.this.width();
				}

				@Override
				public int getPixelHeight() {
					return 16;
				}

				@Override
				public void upload(int i, int j) {
					IntBuffer intBuffer = MemoryUtil.memAllocInt(Glyph.this.width() * 16);
					UnihexProvider.unpackBitsToBytes(intBuffer, Glyph.this.contents, Glyph.this.left, Glyph.this.right);
					intBuffer.rewind();
					GlStateManager.upload(0, i, j, Glyph.this.width(), 16, NativeImage.Format.RGBA, intBuffer, MemoryUtil::memFree);
				}

				@Override
				public boolean isColored() {
					return true;
				}
			});
		}
	}

	@Environment(EnvType.CLIENT)
	static record IntContents(int[] contents, int bitWidth) implements UnihexProvider.LineData {
		private static final int SIZE_24 = 24;

		@Override
		public int line(int i) {
			return this.contents[i];
		}

		static UnihexProvider.LineData read24(int i, ByteList byteList) {
			int[] is = new int[16];
			int j = 0;
			int k = 0;

			for (int l = 0; l < 16; l++) {
				int m = UnihexProvider.decodeHex(i, byteList, k++);
				int n = UnihexProvider.decodeHex(i, byteList, k++);
				int o = UnihexProvider.decodeHex(i, byteList, k++);
				int p = UnihexProvider.decodeHex(i, byteList, k++);
				int q = UnihexProvider.decodeHex(i, byteList, k++);
				int r = UnihexProvider.decodeHex(i, byteList, k++);
				int s = m << 20 | n << 16 | o << 12 | p << 8 | q << 4 | r;
				is[l] = s << 8;
				j |= s;
			}

			return new UnihexProvider.IntContents(is, 24);
		}

		public static UnihexProvider.LineData read32(int i, ByteList byteList) {
			int[] is = new int[16];
			int j = 0;
			int k = 0;

			for (int l = 0; l < 16; l++) {
				int m = UnihexProvider.decodeHex(i, byteList, k++);
				int n = UnihexProvider.decodeHex(i, byteList, k++);
				int o = UnihexProvider.decodeHex(i, byteList, k++);
				int p = UnihexProvider.decodeHex(i, byteList, k++);
				int q = UnihexProvider.decodeHex(i, byteList, k++);
				int r = UnihexProvider.decodeHex(i, byteList, k++);
				int s = UnihexProvider.decodeHex(i, byteList, k++);
				int t = UnihexProvider.decodeHex(i, byteList, k++);
				int u = m << 28 | n << 24 | o << 20 | p << 16 | q << 12 | r << 8 | s << 4 | t;
				is[l] = u;
				j |= u;
			}

			return new UnihexProvider.IntContents(is, 32);
		}
	}

	@Environment(EnvType.CLIENT)
	public interface LineData {
		int line(int i);

		int bitWidth();

		default int mask() {
			int i = 0;

			for (int j = 0; j < 16; j++) {
				i |= this.line(j);
			}

			return i;
		}

		default int calculateWidth() {
			int i = this.mask();
			int j = this.bitWidth();
			int k;
			int l;
			if (i == 0) {
				k = 0;
				l = j;
			} else {
				k = Integer.numberOfLeadingZeros(i);
				l = 32 - Integer.numberOfTrailingZeros(i) - 1;
			}

			return UnihexProvider.Dimensions.pack(k, l);
		}
	}

	@Environment(EnvType.CLIENT)
	static record OverrideRange(int from, int to, UnihexProvider.Dimensions dimensions) {
		private static final Codec<UnihexProvider.OverrideRange> RAW_CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.CODEPOINT.fieldOf("from").forGetter(UnihexProvider.OverrideRange::from),
						ExtraCodecs.CODEPOINT.fieldOf("to").forGetter(UnihexProvider.OverrideRange::to),
						UnihexProvider.Dimensions.MAP_CODEC.forGetter(UnihexProvider.OverrideRange::dimensions)
					)
					.apply(instance, UnihexProvider.OverrideRange::new)
		);
		public static final Codec<UnihexProvider.OverrideRange> CODEC = RAW_CODEC.validate(
			overrideRange -> overrideRange.from >= overrideRange.to
					? DataResult.error(() -> "Invalid range: [" + overrideRange.from + ";" + overrideRange.to + "]")
					: DataResult.success(overrideRange)
		);
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface ReaderOutput {
		void accept(int i, UnihexProvider.LineData lineData);
	}

	@Environment(EnvType.CLIENT)
	static record ShortContents(short[] contents) implements UnihexProvider.LineData {
		@Override
		public int line(int i) {
			return this.contents[i] << 16;
		}

		static UnihexProvider.LineData read(int i, ByteList byteList) {
			short[] ss = new short[16];
			int j = 0;

			for (int k = 0; k < 16; k++) {
				int l = UnihexProvider.decodeHex(i, byteList, j++);
				int m = UnihexProvider.decodeHex(i, byteList, j++);
				int n = UnihexProvider.decodeHex(i, byteList, j++);
				int o = UnihexProvider.decodeHex(i, byteList, j++);
				short s = (short)(l << 12 | m << 8 | n << 4 | o);
				ss[k] = s;
			}

			return new UnihexProvider.ShortContents(ss);
		}

		@Override
		public int bitWidth() {
			return 16;
		}
	}
}
