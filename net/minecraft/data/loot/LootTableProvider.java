/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.loot;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class LootTableProvider
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String name;
    private final PackOutput.PathProvider pathProvider;
    private final Set<ResourceLocation> requiredTables;
    private final List<SubProviderEntry> subProviders;

    public LootTableProvider(String string, PackOutput packOutput, Set<ResourceLocation> set, List<SubProviderEntry> list) {
        this.name = string;
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "loot_tables");
        this.subProviders = list;
        this.requiredTables = set;
    }

    @Override
    public void run(CachedOutput cachedOutput) {
        HashMap<ResourceLocation, LootTable> map = Maps.newHashMap();
        this.subProviders.forEach(subProviderEntry -> subProviderEntry.provider().get().generate((resourceLocation, builder) -> {
            if (map.put((ResourceLocation)resourceLocation, builder.setParamSet(subProviderEntry.paramSet).build()) != null) {
                throw new IllegalStateException("Duplicate loot table " + resourceLocation);
            }
        }));
        ValidationContext validationContext = new ValidationContext(LootContextParamSets.ALL_PARAMS, resourceLocation -> null, map::get);
        Sets.SetView<ResourceLocation> set = Sets.difference(this.requiredTables, map.keySet());
        for (ResourceLocation resourceLocation2 : set) {
            validationContext.reportProblem("Missing built-in table: " + resourceLocation2);
        }
        map.forEach((resourceLocation, lootTable) -> LootTables.validate(validationContext, resourceLocation, lootTable));
        Multimap<String, String> multimap = validationContext.getProblems();
        if (!multimap.isEmpty()) {
            multimap.forEach((string, string2) -> LOGGER.warn("Found validation problem in {}: {}", string, string2));
            throw new IllegalStateException("Failed to validate loot tables, see logs");
        }
        map.forEach((resourceLocation, lootTable) -> {
            Path path = this.pathProvider.json((ResourceLocation)resourceLocation);
            try {
                DataProvider.saveStable(cachedOutput, LootTables.serialize(lootTable), path);
            } catch (IOException iOException) {
                LOGGER.error("Couldn't save loot table {}", (Object)path, (Object)iOException);
            }
        });
    }

    @Override
    public String getName() {
        return this.name;
    }

    public record SubProviderEntry(Supplier<LootTableSubProvider> provider, LootContextParamSet paramSet) {
    }
}

