/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.advancements;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.advancements.AdventureAdvancements;
import net.minecraft.data.advancements.HusbandryAdvancements;
import net.minecraft.data.advancements.NetherAdvancements;
import net.minecraft.data.advancements.StoryAdvancements;
import net.minecraft.data.advancements.TheEndAdvancements;
import org.slf4j.Logger;

public class AdvancementProvider
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DataGenerator.PathProvider pathProvider;
    private final List<Consumer<Consumer<Advancement>>> tabs = ImmutableList.of(new TheEndAdvancements(), new HusbandryAdvancements(), new AdventureAdvancements(), new NetherAdvancements(), new StoryAdvancements());

    public AdvancementProvider(DataGenerator dataGenerator) {
        this.pathProvider = dataGenerator.createPathProvider(DataGenerator.Target.DATA_PACK, "advancements");
    }

    @Override
    public void run(CachedOutput cachedOutput) {
        HashSet set = Sets.newHashSet();
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
        for (Consumer<Consumer<Advancement>> consumer2 : this.tabs) {
            consumer2.accept(consumer);
        }
    }

    @Override
    public String getName() {
        return "Advancements";
    }
}

