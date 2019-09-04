package net.minecraft.world.level.chunk.storage;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;
import javax.annotation.Nullable;

public class RegionFileVersion {
	private static final Int2ObjectMap<RegionFileVersion> VERSIONS = new Int2ObjectOpenHashMap<>();
	public static final RegionFileVersion VERSION_GZIP = register(new RegionFileVersion(1, GZIPInputStream::new, GZIPOutputStream::new));
	public static final RegionFileVersion VERSION_DEFLATE = register(new RegionFileVersion(2, InflaterInputStream::new, DeflaterOutputStream::new));
	public static final RegionFileVersion VERSION_NONE = register(new RegionFileVersion(3, inputStream -> inputStream, outputStream -> outputStream));
	private final int id;
	private final RegionFileVersion.StreamWrapper<InputStream> inputWrapper;
	private final RegionFileVersion.StreamWrapper<OutputStream> outputWrapper;

	private RegionFileVersion(int i, RegionFileVersion.StreamWrapper<InputStream> streamWrapper, RegionFileVersion.StreamWrapper<OutputStream> streamWrapper2) {
		this.id = i;
		this.inputWrapper = streamWrapper;
		this.outputWrapper = streamWrapper2;
	}

	private static RegionFileVersion register(RegionFileVersion regionFileVersion) {
		VERSIONS.put(regionFileVersion.id, regionFileVersion);
		return regionFileVersion;
	}

	@Nullable
	public static RegionFileVersion fromId(int i) {
		return VERSIONS.get(i);
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
