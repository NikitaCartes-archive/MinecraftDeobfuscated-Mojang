/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data;

import com.google.common.base.Stopwatch;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.minecraft.WorldVersion;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.PackOutput;
import net.minecraft.server.Bootstrap;
import org.slf4j.Logger;

public class DataGenerator {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path rootOutputFolder;
    private final PackOutput vanillaPackOutput;
    final Set<String> allProviderIds = new HashSet<String>();
    final Map<String, DataProvider> providersToRun = new LinkedHashMap<String, DataProvider>();
    private final WorldVersion version;
    private final boolean alwaysGenerate;

    public DataGenerator(Path path, WorldVersion worldVersion, boolean bl) {
        this.rootOutputFolder = path;
        this.vanillaPackOutput = new PackOutput(this.rootOutputFolder);
        this.version = worldVersion;
        this.alwaysGenerate = bl;
    }

    public void run() throws IOException {
        HashCache hashCache = new HashCache(this.rootOutputFolder, this.allProviderIds, this.version);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Stopwatch stopwatch2 = Stopwatch.createUnstarted();
        this.providersToRun.forEach((string, dataProvider) -> {
            if (!this.alwaysGenerate && !hashCache.shouldRunInThisVersion((String)string)) {
                LOGGER.debug("Generator {} already run for version {}", string, (Object)this.version.getName());
                return;
            }
            LOGGER.info("Starting provider: {}", string);
            stopwatch2.start();
            hashCache.applyUpdate(hashCache.generateUpdate((String)string, dataProvider::run).join());
            stopwatch2.stop();
            LOGGER.info("{} finished after {} ms", string, (Object)stopwatch2.elapsed(TimeUnit.MILLISECONDS));
            stopwatch2.reset();
        });
        LOGGER.info("All providers took: {} ms", (Object)stopwatch.elapsed(TimeUnit.MILLISECONDS));
        hashCache.purgeStaleAndWrite();
    }

    public PackGenerator getVanillaPack(boolean bl) {
        return new PackGenerator(bl, "vanilla", this.vanillaPackOutput);
    }

    public PackGenerator getBuiltinDatapack(boolean bl, String string) {
        Path path = this.vanillaPackOutput.getOutputFolder(PackOutput.Target.DATA_PACK).resolve("minecraft").resolve("datapacks").resolve(string);
        return new PackGenerator(bl, string, new PackOutput(path));
    }

    static {
        Bootstrap.bootStrap();
    }

    public class PackGenerator {
        private final boolean toRun;
        private final String providerPrefix;
        private final PackOutput output;

        PackGenerator(boolean bl, String string, PackOutput packOutput) {
            this.toRun = bl;
            this.providerPrefix = string;
            this.output = packOutput;
        }

        public <T extends DataProvider> T addProvider(DataProvider.Factory<T> factory) {
            T dataProvider = factory.create(this.output);
            String string = this.providerPrefix + "/" + dataProvider.getName();
            if (!DataGenerator.this.allProviderIds.add(string)) {
                throw new IllegalStateException("Duplicate provider: " + string);
            }
            if (this.toRun) {
                DataGenerator.this.providersToRun.put(string, (DataProvider)dataProvider);
            }
            return dataProvider;
        }
    }
}

