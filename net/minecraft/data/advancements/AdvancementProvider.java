/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.advancements;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementSubProvider;
import org.slf4j.Logger;

public class AdvancementProvider
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String name;
    private final PackOutput.PathProvider pathProvider;
    private final List<AdvancementSubProvider> subProviders;

    public AdvancementProvider(String string, PackOutput packOutput, List<AdvancementSubProvider> list) {
        this.name = string;
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "advancements");
        this.subProviders = list;
    }

    @Override
    public void run(CachedOutput cachedOutput) {
        HashSet set = new HashSet();
        Consumer<Advancement> consumer = advancement -> {
            if (!set.add(advancement.getId())) {
                throw new IllegalStateException("Duplicate advancement " + advancement.getId());
            }
            Path path = this.pathProvider.json(advancement.getId());
            try {
                DataProvider.saveStable(cachedOutput, advancement.deconstruct().serializeToJson(), path);
            } catch (IOException iOException) {
                LOGGER.error("Couldn't save advancement {}", (Object)path, (Object)iOException);
            }
        };
        for (AdvancementSubProvider advancementSubProvider : this.subProviders) {
            advancementSubProvider.generate(consumer);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }
}

