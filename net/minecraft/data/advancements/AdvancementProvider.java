/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.advancements;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.advancements.AdventureAdvancements;
import net.minecraft.data.advancements.HusbandryAdvancements;
import net.minecraft.data.advancements.NetherAdvancements;
import net.minecraft.data.advancements.StoryAdvancements;
import net.minecraft.data.advancements.TheEndAdvancements;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdvancementProvider
implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final DataGenerator generator;
    private final List<Consumer<Consumer<Advancement>>> tabs = ImmutableList.of(new TheEndAdvancements(), new HusbandryAdvancements(), new AdventureAdvancements(), new NetherAdvancements(), new StoryAdvancements());

    public AdvancementProvider(DataGenerator dataGenerator) {
        this.generator = dataGenerator;
    }

    @Override
    public void run(HashCache hashCache) {
        Path path = this.generator.getOutputFolder();
        HashSet set = Sets.newHashSet();
        Consumer<Advancement> consumer = advancement -> {
            if (!set.add(advancement.getId())) {
                throw new IllegalStateException("Duplicate advancement " + advancement.getId());
            }
            Path path2 = AdvancementProvider.createPath(path, advancement);
            try {
                DataProvider.save(GSON, hashCache, advancement.deconstruct().serializeToJson(), path2);
            } catch (IOException iOException) {
                LOGGER.error("Couldn't save advancement {}", (Object)path2, (Object)iOException);
            }
        };
        for (Consumer<Consumer<Advancement>> consumer2 : this.tabs) {
            consumer2.accept(consumer);
        }
    }

    private static Path createPath(Path path, Advancement advancement) {
        return path.resolve("data/" + advancement.getId().getNamespace() + "/advancements/" + advancement.getId().getPath() + ".json");
    }

    @Override
    public String getName() {
        return "Advancements";
    }
}

