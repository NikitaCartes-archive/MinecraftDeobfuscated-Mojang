package net.minecraft.world.level.chunk.storage;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;
import javax.annotation.Nullable;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.minecraft.util.FastBufferedInputStream;
import org.slf4j.Logger;

public class RegionFileVersion {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Int2ObjectMap<RegionFileVersion> VERSIONS = new Int2ObjectOpenHashMap<>();
	private static final Object2ObjectMap<String, RegionFileVersion> VERSIONS_BY_NAME = new Object2ObjectOpenHashMap<>();
	public static final RegionFileVersion VERSION_GZIP = register(
		new RegionFileVersion(
			1,
			null,
			inputStream -> new FastBufferedInputStream(new GZIPInputStream(inputStream)),
			outputStream -> new BufferedOutputStream(new GZIPOutputStream(outputStream))
		)
	);
	public static final RegionFileVersion VERSION_DEFLATE = register(
		new RegionFileVersion(
			2,
			"deflate",
			inputStream -> new FastBufferedInputStream(new InflaterInputStream(inputStream)),
			outputStream -> new BufferedOutputStream(new DeflaterOutputStream(outputStream))
		)
	);
	public static final RegionFileVersion VERSION_NONE = register(new RegionFileVersion(3, "none", FastBufferedInputStream::new, BufferedOutputStream::new));
	public static final RegionFileVersion VERSION_LZ4 = register(
		new RegionFileVersion(
			4,
			"lz4",
			inputStream -> new FastBufferedInputStream(new LZ4BlockInputStream(inputStream)),
			outputStream -> new BufferedOutputStream(new LZ4BlockOutputStream(outputStream))
		)
	);
	public static final RegionFileVersion VERSION_CUSTOM = register(new RegionFileVersion(127, null, inputStream -> {
		throw new UnsupportedOperationException();
	}, outputStream -> {
		throw new UnsupportedOperationException();
	}));
	public static final RegionFileVersion DEFAULT = VERSION_DEFLATE;
	private static volatile RegionFileVersion selected = DEFAULT;
	private final int id;
	@Nullable
	private final String optionName;
	private final RegionFileVersion.StreamWrapper<InputStream> inputWrapper;
	private final RegionFileVersion.StreamWrapper<OutputStream> outputWrapper;

	private RegionFileVersion(
		int i, @Nullable String string, RegionFileVersion.StreamWrapper<InputStream> streamWrapper, RegionFileVersion.StreamWrapper<OutputStream> streamWrapper2
	) {
		this.id = i;
		this.optionName = string;
		this.inputWrapper = streamWrapper;
		this.outputWrapper = streamWrapper2;
	}

	private static RegionFileVersion register(RegionFileVersion regionFileVersion) {
		VERSIONS.put(regionFileVersion.id, regionFileVersion);
		if (regionFileVersion.optionName != null) {
			VERSIONS_BY_NAME.put(regionFileVersion.optionName, regionFileVersion);
		}

		return regionFileVersion;
	}

	@Nullable
	public static RegionFileVersion fromId(int i) {
		return VERSIONS.get(i);
	}

	public static void configure(String string) {
		RegionFileVersion regionFileVersion = VERSIONS_BY_NAME.get(string);
		if (regionFileVersion != null) {
			selected = regionFileVersion;
		} else {
			LOGGER.error(
				"Invalid `region-file-compression` value `{}` in server.properties. Please use one of: {}", string, String.join(", ", VERSIONS_BY_NAME.keySet())
			);
		}
	}

	public static RegionFileVersion getSelected() {
		return selected;
	}

	public static boolean isValidVersion(int i) {
		return VERSIONS.containsKey(i);
	}

	public int getId() {
		return this.id;
	}

	public OutputStream wrap(OutputStream outputStream) throws IOException {
		return this.outputWrapper.wrap(outputStream);
	}

	public InputStream wrap(InputStream inputStream) throws IOException {
		return this.inputWrapper.wrap(inputStream);
	}

	@FunctionalInterface
	interface StreamWrapper<O> {
		O wrap(O object) throws IOException;
	}
}
