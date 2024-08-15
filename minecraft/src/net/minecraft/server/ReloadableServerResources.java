package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.Unit;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import org.slf4j.Logger;

public class ReloadableServerResources {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final CompletableFuture<Unit> DATA_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
	private final ReloadableServerRegistries.Holder fullRegistryHolder;
	private final Commands commands;
	private final RecipeManager recipes;
	private final ServerAdvancementManager advancements;
	private final ServerFunctionLibrary functionLibrary;
	private final List<Registry.PendingTags<?>> postponedTags;

	private ReloadableServerResources(
		LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess,
		HolderLookup.Provider provider,
		FeatureFlagSet featureFlagSet,
		Commands.CommandSelection commandSelection,
		List<Registry.PendingTags<?>> list,
		int i
	) {
		this.fullRegistryHolder = new ReloadableServerRegistries.Holder(layeredRegistryAccess.compositeAccess());
		this.postponedTags = list;
		this.recipes = new RecipeManager(provider);
		this.commands = new Commands(commandSelection, CommandBuildContext.simple(provider, featureFlagSet));
		this.advancements = new ServerAdvancementManager(provider);
		this.functionLibrary = new ServerFunctionLibrary(i, this.commands.getDispatcher());
	}

	public ServerFunctionLibrary getFunctionLibrary() {
		return this.functionLibrary;
	}

	public ReloadableServerRegistries.Holder fullRegistries() {
		return this.fullRegistryHolder;
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
		return List.of(this.recipes, this.functionLibrary, this.advancements);
	}

	public static CompletableFuture<ReloadableServerResources> loadResources(
		ResourceManager resourceManager,
		LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess,
		List<Registry.PendingTags<?>> list,
		FeatureFlagSet featureFlagSet,
		Commands.CommandSelection commandSelection,
		int i,
		Executor executor,
		Executor executor2
	) {
		return ReloadableServerRegistries.reload(layeredRegistryAccess, list, resourceManager, executor)
			.thenCompose(
				loadResult -> {
					ReloadableServerResources reloadableServerResources = new ReloadableServerResources(
						loadResult.layers(), loadResult.lookupWithUpdatedTags(), featureFlagSet, commandSelection, list, i
					);
					return SimpleReloadInstance.create(
							resourceManager, reloadableServerResources.listeners(), executor, executor2, DATA_RELOAD_INITIAL_TASK, LOGGER.isDebugEnabled()
						)
						.done()
						.thenApply(object -> reloadableServerResources);
				}
			);
	}

	public void updateStaticRegistryTags() {
		this.postponedTags.forEach(Registry.PendingTags::apply);
	}
}
