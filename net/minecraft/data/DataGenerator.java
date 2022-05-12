/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.WorldVersion;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import org.slf4j.Logger;

public class DataGenerator {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Collection<Path> inputFolders;
    private final Path outputFolder;
    private final List<DataProvider> allProviders = Lists.newArrayList();
    private final List<DataProvider> providersToRun = Lists.newArrayList();
    private final WorldVersion version;
    private final boolean alwaysGenerate;

    public DataGenerator(Path path, Collection<Path> collection, WorldVersion worldVersion, boolean bl) {
        this.outputFolder = path;
        this.inputFolders = collection;
        this.version = worldVersion;
        this.alwaysGenerate = bl;
    }

    public Collection<Path> getInputFolders() {
        return this.inputFolders;
    }

    public Path getOutputFolder() {
        return this.outputFolder;
    }

    public Path getOutputFolder(Target target) {
        return this.getOutputFolder().resolve(target.directory);
    }

    public void run() throws IOException {
        HashCache hashCache = new HashCache(this.outputFolder, this.allProviders, this.version);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Stopwatch stopwatch2 = Stopwatch.createUnstarted();
        for (DataProvider dataProvider : this.providersToRun) {
            if (!this.alwaysGenerate && !hashCache.shouldRunInThisVersion(dataProvider)) {
                LOGGER.debug("Generator {} already run for version {}", (Object)dataProvider.getName(), (Object)this.version.getName());
                continue;
            }
            LOGGER.info("Starting provider: {}", (Object)dataProvider.getName());
            stopwatch2.start();
            dataProvider.run(hashCache.getUpdater(dataProvider));
            stopwatch2.stop();
            LOGGER.info("{} finished after {} ms", (Object)dataProvider.getName(), (Object)stopwatch2.elapsed(TimeUnit.MILLISECONDS));
            stopwatch2.reset();
        }
        LOGGER.info("All providers took: {} ms", (Object)stopwatch.elapsed(TimeUnit.MILLISECONDS));
        hashCache.purgeStaleAndWrite();
    }

    public void addProvider(boolean bl, DataProvider dataProvider) {
        if (bl) {
            this.providersToRun.add(dataProvider);
        }
        this.allProviders.add(dataProvider);
    }

    public PathProvider createPathProvider(Target target, String string) {
        return new PathProvider(this, target, string);
    }

    static {
        Bootstrap.bootStrap();
    }

    public static enum Target {
        DATA_PACK("data"),
        RESOURCE_PACK("assets"),
        REPORTS("reports");

        final String directory;

        private Target(String string2) {
            this.directory = string2;
        }
    }

    public static class PathProvider {
        private final Path root;
        private final String kind;

        PathProvider(DataGenerator dataGenerator, Target target, String string) {
            this.root = dataGenerator.getOutputFolder(target);
            this.kind = string;
        }

        public Path file(ResourceLocation resourceLocation, String string) {
            return this.root.resolve(resourceLocation.getNamespace()).resolve(this.kind).resolve(resourceLocation.getPath() + "." + string);
        }

        public Path json(ResourceLocation resourceLocation) {
            return this.root.resolve(resourceLocation.getNamespace()).resolve(this.kind).resolve(resourceLocation.getPath() + ".json");
        }
    }
}

