/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.ServerFunctionLibrary;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.util.Unit;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.ItemModifierManager;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.PredicateManager;
import org.slf4j.Logger;

public class ReloadableServerResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CompletableFuture<Unit> DATA_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
    private final CommandBuildContext.Configurable commandBuildContext;
    private final Commands commands;
    private final RecipeManager recipes = new RecipeManager();
    private final TagManager tagManager;
    private final PredicateManager predicateManager = new PredicateManager();
    private final LootTables lootTables = new LootTables(this.predicateManager);
    private final ItemModifierManager itemModifierManager = new ItemModifierManager(this.predicateManager, this.lootTables);
    private final ServerAdvancementManager advancements = new ServerAdvancementManager(this.predicateManager);
    private final ServerFunctionLibrary functionLibrary;

    public ReloadableServerResources(RegistryAccess.Frozen frozen, FeatureFlagSet featureFlagSet, Commands.CommandSelection commandSelection, int i) {
        this.tagManager = new TagManager(frozen);
        this.commandBuildContext = CommandBuildContext.configurable(frozen, featureFlagSet);
        this.commands = new Commands(commandSelection, this.commandBuildContext);
        this.commandBuildContext.missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy.CREATE_NEW);
        this.functionLibrary = new ServerFunctionLibrary(i, this.commands.getDispatcher());
    }

    public ServerFunctionLibrary getFunctionLibrary() {
        return this.functionLibrary;
    }

    public PredicateManager getPredicateManager() {
        return this.predicateManager;
    }

    public LootTables getLootTables() {
        return this.lootTables;
    }

    public ItemModifierManager getItemModifierManager() {
        return this.itemModifierManager;
    }

    public RecipeManager getRecipeManager() {
        return this.recipes;
    }

    public Commands getCommands() {
        return this.commands;
    }

    public ServerAdvancementManager getAdvancements() {
        return this.advancements;
    }

    public List<PreparableReloadListener> listeners() {
        return List.of(this.tagManager, this.predicateManager, this.recipes, this.lootTables, this.itemModifierManager, this.functionLibrary, this.advancements);
    }

    public static CompletableFuture<ReloadableServerResources> loadResources(ResourceManager resourceManager, RegistryAccess.Frozen frozen, FeatureFlagSet featureFlagSet, Commands.CommandSelection commandSelection, int i, Executor executor, Executor executor2) {
        ReloadableServerResources reloadableServerResources = new ReloadableServerResources(frozen, featureFlagSet, commandSelection, i);
        return ((CompletableFuture)SimpleReloadInstance.create(resourceManager, reloadableServerResources.listeners(), executor, executor2, DATA_RELOAD_INITIAL_TASK, LOGGER.isDebugEnabled()).done().whenComplete((object, throwable) -> reloadableServerResources.commandBuildContext.missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy.FAIL))).thenApply(object -> reloadableServerResources);
    }

    public void updateRegistryTags(RegistryAccess registryAccess) {
        this.tagManager.getResult().forEach(loadResult -> ReloadableServerResources.updateRegistryTags(registryAccess, loadResult));
        Blocks.rebuildCache();
    }

    private static <T> void updateRegistryTags(RegistryAccess registryAccess, TagManager.LoadResult<T> loadResult) {
        ResourceKey resourceKey = loadResult.key();
        Map map = loadResult.tags().entrySet().stream().collect(Collectors.toUnmodifiableMap(entry -> TagKey.create(resourceKey, (ResourceLocation)entry.getKey()), entry -> List.copyOf((Collection)entry.getValue())));
        registryAccess.registryOrThrow(resourceKey).bindTags(map);
    }
}

