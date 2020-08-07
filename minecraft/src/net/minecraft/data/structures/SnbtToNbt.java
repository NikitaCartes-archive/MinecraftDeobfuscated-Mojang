package net.minecraft.data.structures;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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
import net.minecraft.nbt.TagParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SnbtToNbt implements DataProvider {
	@Nullable
	private static final Path dumpSnbtTo = null;
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

		((List)Util.sequence(list).join()).stream().filter(Objects::nonNull).forEach(taskResult -> this.storeStructureIfChanged(hashCache, taskResult, path));
	}

	@Override
	public String getName() {
		return "SNBT -> NBT";
	}

	private String getName(Path path, Path path2) {
		String string = path.relativize(path2).toString().replaceAll("\\\\", "/");
		return string.substring(0, string.length() - ".snbt".length());
	}

	@Nullable
	private SnbtToNbt.TaskResult readStructure(Path path, String string) {
		try {
			BufferedReader bufferedReader = Files.newBufferedReader(path);
			Throwable var4 = null;

			SnbtToNbt.TaskResult var11;
			try {
				String string2 = IOUtils.toString(bufferedReader);
				CompoundTag compoundTag = this.applyFilters(string, TagParser.parseTag(string2));
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				NbtIo.writeCompressed(compoundTag, byteArrayOutputStream);
				byte[] bs = byteArrayOutputStream.toByteArray();
				String string3 = SHA1.hashBytes(bs).toString();
				String string4;
				if (dumpSnbtTo != null) {
					string4 = compoundTag.getPrettyDisplay("    ", 0).getString() + "\n";
				} else {
					string4 = null;
				}

				var11 = new SnbtToNbt.TaskResult(string, bs, string4, string3);
			} catch (Throwable var22) {
				var4 = var22;
				throw var22;
			} finally {
				if (bufferedReader != null) {
					if (var4 != null) {
						try {
							bufferedReader.close();
						} catch (Throwable var21) {
							var4.addSuppressed(var21);
						}
					} else {
						bufferedReader.close();
					}
				}
			}

			return var11;
		} catch (CommandSyntaxException var24) {
			LOGGER.error("Couldn't convert {} from SNBT to NBT at {} as it's invalid SNBT", string, path, var24);
		} catch (IOException var25) {
			LOGGER.error("Couldn't convert {} from SNBT to NBT at {}", string, path, var25);
		}

		return null;
	}

	private void storeStructureIfChanged(HashCache hashCache, SnbtToNbt.TaskResult taskResult, Path path) {
		if (taskResult.snbtPayload != null) {
			Path path2 = dumpSnbtTo.resolve(taskResult.name + ".snbt");

			try {
				FileUtils.write(path2.toFile(), taskResult.snbtPayload, StandardCharsets.UTF_8);
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
