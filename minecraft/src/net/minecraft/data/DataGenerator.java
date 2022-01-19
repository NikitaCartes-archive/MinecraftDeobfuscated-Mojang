package net.minecraft.data;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.server.Bootstrap;
import org.slf4j.Logger;

public class DataGenerator {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Collection<Path> inputFolders;
	private final Path outputFolder;
	private final List<DataProvider> providers = Lists.<DataProvider>newArrayList();

	public DataGenerator(Path path, Collection<Path> collection) {
		this.outputFolder = path;
		this.inputFolders = collection;
	}

	public Collection<Path> getInputFolders() {
		return this.inputFolders;
	}

	public Path getOutputFolder() {
		return this.outputFolder;
	}

	public void run() throws IOException {
		HashCache hashCache = new HashCache(this.outputFolder, "cache");
		hashCache.keep(this.getOutputFolder().resolve("version.json"));
		Stopwatch stopwatch = Stopwatch.createStarted();
		Stopwatch stopwatch2 = Stopwatch.createUnstarted();

		for (DataProvider dataProvider : this.providers) {
			LOGGER.info("Starting provider: {}", dataProvider.getName());
			stopwatch2.start();
			dataProvider.run(hashCache);
			stopwatch2.stop();
			LOGGER.info("{} finished after {} ms", dataProvider.getName(), stopwatch2.elapsed(TimeUnit.MILLISECONDS));
			stopwatch2.reset();
		}

		LOGGER.info("All providers took: {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
		hashCache.purgeStaleAndWrite();
	}

	public void addProvider(DataProvider dataProvider) {
		this.providers.add(dataProvider);
	}

	static {
		Bootstrap.bootStrap();
	}
}
