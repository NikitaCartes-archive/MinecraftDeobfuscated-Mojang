package net.minecraft.data.structures;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SnbtToNbt implements DataProvider {
	private static final Logger LOGGER = LogManager.getLogger();
	private final DataGenerator generator;
	private final List<SnbtToNbt.Filter> filters = Lists.<SnbtToNbt.Filter>newArrayList();

	public SnbtToNbt(DataGenerator dataGenerator) {
		this.generator = dataGenerator;
	}

	public SnbtToNbt addFilter(SnbtToNbt.Filter filter) {
		this.filters.add(filter);
		return this;
	}

	private CompoundTag applyFilters(String string, CompoundTag compoundTag) {
		CompoundTag compoundTag2 = compoundTag;

		for (SnbtToNbt.Filter filter : this.filters) {
			compoundTag2 = filter.apply(string, compoundTag2);
		}

		return compoundTag2;
	}

	@Override
	public void run(HashCache hashCache) throws IOException {
		Path path = this.generator.getOutputFolder();

		for (Path path2 : this.generator.getInputFolders()) {
			Files.walk(path2)
				.filter(pathx -> pathx.toString().endsWith(".snbt"))
				.forEach(path3 -> this.convertStructure(hashCache, path3, this.getName(path2, path3), path));
		}
	}

	@Override
	public String getName() {
		return "SNBT -> NBT";
	}

	private String getName(Path path, Path path2) {
		String string = path.relativize(path2).toString().replaceAll("\\\\", "/");
		return string.substring(0, string.length() - ".snbt".length());
	}

	private void convertStructure(HashCache hashCache, Path path, String string, Path path2) {
		try {
			Path path3 = path2.resolve(string + ".nbt");
			BufferedReader bufferedReader = Files.newBufferedReader(path);
			Throwable var7 = null;

			try {
				String string2 = IOUtils.toString(bufferedReader);
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				NbtIo.writeCompressed(this.applyFilters(string, TagParser.parseTag(string2)), byteArrayOutputStream);
				String string3 = SHA1.hashBytes(byteArrayOutputStream.toByteArray()).toString();
				if (!Objects.equals(hashCache.getHash(path3), string3) || !Files.exists(path3, new LinkOption[0])) {
					Files.createDirectories(path3.getParent());
					OutputStream outputStream = Files.newOutputStream(path3);
					Throwable var12 = null;

					try {
						outputStream.write(byteArrayOutputStream.toByteArray());
					} catch (Throwable var39) {
						var12 = var39;
						throw var39;
					} finally {
						if (outputStream != null) {
							if (var12 != null) {
								try {
									outputStream.close();
								} catch (Throwable var38) {
									var12.addSuppressed(var38);
								}
							} else {
								outputStream.close();
							}
						}
					}
				}

				hashCache.putNew(path3, string3);
			} catch (Throwable var41) {
				var7 = var41;
				throw var41;
			} finally {
				if (bufferedReader != null) {
					if (var7 != null) {
						try {
							bufferedReader.close();
						} catch (Throwable var37) {
							var7.addSuppressed(var37);
						}
					} else {
						bufferedReader.close();
					}
				}
			}
		} catch (CommandSyntaxException var43) {
			LOGGER.error("Couldn't convert {} from SNBT to NBT at {} as it's invalid SNBT", string, path, var43);
		} catch (IOException var44) {
			LOGGER.error("Couldn't convert {} from SNBT to NBT at {}", string, path, var44);
		}
	}

	@FunctionalInterface
	public interface Filter {
		CompoundTag apply(String string, CompoundTag compoundTag);
	}
}
