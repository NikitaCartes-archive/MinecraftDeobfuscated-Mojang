package net.minecraft.data.structures;

import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SnbtToNbt implements DataProvider {
	@Nullable
	private static final Path DUMP_SNBT_TO = null;
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
		List<CompletableFuture<SnbtToNbt.TaskResult>> list = Lists.<CompletableFuture<SnbtToNbt.TaskResult>>newArrayList();

		for (Path path2 : this.generator.getInputFolders()) {
			Files.walk(path2)
				.filter(pathx -> pathx.toString().endsWith(".snbt"))
				.forEach(path2x -> list.add(CompletableFuture.supplyAsync(() -> this.readStructure(path2x, this.getName(path2, path2x)), Util.backgroundExecutor())));
		}

		boolean bl = false;

		for (CompletableFuture<SnbtToNbt.TaskResult> completableFuture : list) {
			try {
				this.storeStructureIfChanged(hashCache, (SnbtToNbt.TaskResult)completableFuture.get(), path);
			} catch (Exception var8) {
				LOGGER.error("Failed to process structure", (Throwable)var8);
				bl = true;
			}
		}

		if (bl) {
			throw new IllegalStateException("Failed to convert all structures, aborting");
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

	private SnbtToNbt.TaskResult readStructure(Path path, String string) {
		try {
			BufferedReader bufferedReader = Files.newBufferedReader(path);
			Throwable var4 = null;

			SnbtToNbt.TaskResult var11;
			try {
				String string2 = IOUtils.toString(bufferedReader);
				CompoundTag compoundTag = this.applyFilters(string, NbtUtils.snbtToStructure(string2));
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				NbtIo.writeCompressed(compoundTag, byteArrayOutputStream);
				byte[] bs = byteArrayOutputStream.toByteArray();
				String string3 = SHA1.hashBytes(bs).toString();
				String string4;
				if (DUMP_SNBT_TO != null) {
					string4 = NbtUtils.structureToSnbt(compoundTag);
				} else {
					string4 = null;
				}

				var11 = new SnbtToNbt.TaskResult(string, bs, string4, string3);
			} catch (Throwable var21) {
				var4 = var21;
				throw var21;
			} finally {
				if (bufferedReader != null) {
					if (var4 != null) {
						try {
							bufferedReader.close();
						} catch (Throwable var20) {
							var4.addSuppressed(var20);
						}
					} else {
						bufferedReader.close();
					}
				}
			}

			return var11;
		} catch (Throwable var23) {
			throw new SnbtToNbt.StructureConversionException(path, var23);
		}
	}

	private void storeStructureIfChanged(HashCache hashCache, SnbtToNbt.TaskResult taskResult, Path path) {
		if (taskResult.snbtPayload != null) {
			Path path2 = DUMP_SNBT_TO.resolve(taskResult.name + ".snbt");

			try {
				NbtToSnbt.writeSnbt(path2, taskResult.snbtPayload);
			} catch (IOException var18) {
				LOGGER.error("Couldn't write structure SNBT {} at {}", taskResult.name, path2, var18);
			}
		}

		Path path2 = path.resolve(taskResult.name + ".nbt");

		try {
			if (!Objects.equals(hashCache.getHash(path2), taskResult.hash) || !Files.exists(path2, new LinkOption[0])) {
				Files.createDirectories(path2.getParent());
				OutputStream outputStream = Files.newOutputStream(path2);
				Throwable var6 = null;

				try {
					outputStream.write(taskResult.payload);
				} catch (Throwable var17) {
					var6 = var17;
					throw var17;
				} finally {
					if (outputStream != null) {
						if (var6 != null) {
							try {
								outputStream.close();
							} catch (Throwable var16) {
								var6.addSuppressed(var16);
							}
						} else {
							outputStream.close();
						}
					}
				}
			}

			hashCache.putNew(path2, taskResult.hash);
		} catch (IOException var20) {
			LOGGER.error("Couldn't write structure {} at {}", taskResult.name, path2, var20);
		}
	}

	@FunctionalInterface
	public interface Filter {
		CompoundTag apply(String string, CompoundTag compoundTag);
	}

	static class StructureConversionException extends RuntimeException {
		public StructureConversionException(Path path, Throwable throwable) {
			super(path.toAbsolutePath().toString(), throwable);
		}
	}

	static class TaskResult {
		private final String name;
		private final byte[] payload;
		@Nullable
		private final String snbtPayload;
		private final String hash;

		public TaskResult(String string, byte[] bs, @Nullable String string2, String string3) {
			this.name = string;
			this.payload = bs;
			this.snbtPayload = string2;
			this.hash = string3;
		}
	}
}
