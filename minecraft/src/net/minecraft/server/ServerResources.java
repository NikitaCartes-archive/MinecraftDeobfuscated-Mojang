package net.minecraft.server;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.tags.TagContainer;
import net.minecraft.tags.TagManager;
import net.minecraft.util.Unit;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.loot.ItemModifierManager;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.PredicateManager;

public class ServerResources implements AutoCloseable {
	private static final CompletableFuture<Unit> DATA_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
	private final ReloadableResourceManager resources = new SimpleReloadableResourceManager(PackType.SERVER_DATA);
	private final Commands commands;
	private final RecipeManager recipes = new RecipeManager();
	private final TagManager tagManager;
	private final PredicateManager predicateManager = new PredicateManager();
	private final LootTables lootTables = new LootTables(this.predicateManager);
	private final ItemModifierManager itemModifierManager = new ItemModifierManager(this.predicateManager, this.lootTables);
	private final ServerAdvancementManager advancements = new ServerAdvancementManager(this.predicateManager);
	private final ServerFunctionLibrary functionLibrary;

	public ServerResources(RegistryAccess registryAccess, Commands.CommandSelection commandSelection, int i) {
		this.tagManager = new TagManager(registryAccess);
		this.commands = new Commands(commandSelection);
		this.functionLibrary = new ServerFunctionLibrary(i, this.commands.getDispatcher());
		this.resources.registerReloadListener(this.tagManager);
		this.resources.registerReloadListener(this.predicateManager);
		this.resources.registerReloadListener(this.recipes);
		this.resources.registerReloadListener(this.lootTables);
		this.resources.registerReloadListener(this.itemModifierManager);
		this.resources.registerReloadListener(this.functionLibrary);
		this.resources.registerReloadListener(this.advancements);
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

	public TagContainer getTags() {
		return this.tagManager.getTags();
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

	public ResourceManager getResourceManager() {
		return this.resources;
	}

	public static CompletableFuture<ServerResources> loadResources(
		List<PackResources> list, RegistryAccess registryAccess, Commands.CommandSelection commandSelection, int i, Executor executor, Executor executor2
	) {
		ServerResources serverResources = new ServerResources(registryAccess, commandSelection, i);
		CompletableFuture<Unit> completableFuture = serverResources.resources.reload(executor, executor2, list, DATA_RELOAD_INITIAL_TASK);
		return completableFuture.whenComplete((unit, throwable) -> {
			if (throwable != null) {
				serverResources.close();
			}
		}).thenApply(unit -> serverResources);
	}

	public void updateGlobals() {
		this.tagManager.getTags().bindToGlobal();
	}

	public void close() {
		this.resources.close();
	}
}
