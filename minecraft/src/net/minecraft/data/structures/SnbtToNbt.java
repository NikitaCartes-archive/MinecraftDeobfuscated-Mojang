package net.minecraft.data.structures;

import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class SnbtToNbt implements DataProvider {
	@Nullable
	private static final Path DUMP_SNBT_TO = null;
	private static final Logger LOGGER = LogUtils.getLogger();
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
	public void run(CachedOutput cachedOutput) throws IOException {
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
				this.storeStructureIfChanged(cachedOutput, (SnbtToNbt.TaskResult)completableFuture.get(), path);
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

			SnbtToNbt.TaskResult var11;
			try {
				String string2 = IOUtils.toString(bufferedReader);
				CompoundTag compoundTag = this.applyFilters(string, NbtUtils.snbtToStructure(string2));
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), byteArrayOutputStream);
				NbtIo.writeCompressed(compoundTag, hashingOutputStream);
				byte[] bs = byteArrayOutputStream.toByteArray();
				HashCode hashCode = hashingOutputStream.hash();
				String string3;
				if (DUMP_SNBT_TO != null) {
					string3 = NbtUtils.structureToSnbt(compoundTag);
				} else {
					string3 = null;
				}

				var11 = new SnbtToNbt.TaskResult(string, bs, string3, hashCode);
			} catch (Throwable var13) {
				if (bufferedReader != null) {
					try {
						bufferedReader.close();
					} catch (Throwable var12) {
						var13.addSuppressed(var12);
					}
				}

				throw var13;
			}

			if (bufferedReader != null) {
				bufferedReader.close();
			}

			return var11;
		} catch (Throwable var14) {
			throw new SnbtToNbt.StructureConversionException(path, var14);
		}
	}

	private void storeStructureIfChanged(CachedOutput cachedOutput, SnbtToNbt.TaskResult taskResult, Path path) {
		if (taskResult.snbtPayload != null) {
			Path path2 = DUMP_SNBT_TO.resolve(taskResult.name + ".snbt");

			try {
				NbtToSnbt.writeSnbt(CachedOutput.NO_CACHE, path2, taskResult.snbtPayload);
			} catch (IOException var7) {
				LOGGER.error("Couldn't write structure SNBT {} at {}", taskResult.name, path2, var7);
			}
		}

		Path path2 = path.resolve(taskResult.name + ".nbt");

		try {
			cachedOutput.writeIfNeeded(path2, taskResult.payload, taskResult.hash);
		} catch (IOException var6) {
			LOGGER.error("Couldn't write structure {} at {}", taskResult.name, path2, var6);
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

	static record TaskResult(String name, byte[] payload, @Nullable String snbtPayload, HashCode hash) {
	}
}
