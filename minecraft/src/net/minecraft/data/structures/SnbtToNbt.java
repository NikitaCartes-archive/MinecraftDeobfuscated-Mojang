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
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
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

			SnbtToNbt.TaskResult var9;
			try {
				String string2 = IOUtils.toString(bufferedReader);
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				NbtIo.writeCompressed(this.applyFilters(string, TagParser.parseTag(string2)), byteArrayOutputStream);
				byte[] bs = byteArrayOutputStream.toByteArray();
				String string3 = SHA1.hashBytes(bs).toString();
				var9 = new SnbtToNbt.TaskResult(string, bs, string3);
			} catch (Throwable var20) {
				var4 = var20;
				throw var20;
			} finally {
				if (bufferedReader != null) {
					if (var4 != null) {
						try {
							bufferedReader.close();
						} catch (Throwable var19) {
							var4.addSuppressed(var19);
						}
					} else {
						bufferedReader.close();
					}
				}
			}

			return var9;
		} catch (CommandSyntaxException var22) {
			LOGGER.error("Couldn't convert {} from SNBT to NBT at {} as it's invalid SNBT", string, path, var22);
		} catch (IOException var23) {
			LOGGER.error("Couldn't convert {} from SNBT to NBT at {}", string, path, var23);
		}

		return null;
	}

	private void storeStructureIfChanged(HashCache hashCache, SnbtToNbt.TaskResult taskResult, Path path) {
		Path path2 = path.resolve(taskResult.name + ".nbt");

		try {
			if (!Objects.equals(hashCache.getHash(path2), taskResult.hash) || !Files.exists(path2, new LinkOption[0])) {
				Files.createDirectories(path2.getParent());
				OutputStream outputStream = Files.newOutputStream(path2);
				Throwable var6 = null;

				try {
					outputStream.write(taskResult.payload);
				} catch (Throwable var16) {
					var6 = var16;
					throw var16;
				} finally {
					if (outputStream != null) {
						if (var6 != null) {
							try {
								outputStream.close();
							} catch (Throwable var15) {
								var6.addSuppressed(var15);
							}
						} else {
							outputStream.close();
						}
					}
				}
			}

			hashCache.putNew(path2, taskResult.hash);
		} catch (IOException var18) {
			LOGGER.error("Couldn't write structure {} at {}", taskResult.name, path2, var18);
		}
	}

	@FunctionalInterface
	public interface Filter {
		CompoundTag apply(String string, CompoundTag compoundTag);
	}

	static class TaskResult {
		private final String name;
		private final byte[] payload;
		private final String hash;

		public TaskResult(String string, byte[] bs, String string2) {
			this.name = string;
			this.payload = bs;
			this.hash = string2;
		}
	}
}
