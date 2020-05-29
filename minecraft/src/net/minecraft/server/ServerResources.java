package net.minecraft.server;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.commands.Commands;
import net.minecraft.server.packs.Pack;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.tags.TagManager;
import net.minecraft.util.Unit;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.PredicateManager;

public class ServerResources {
	private static final CompletableFuture<Unit> DATA_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
	private final ReloadableResourceManager resources = new SimpleReloadableResourceManager(PackType.SERVER_DATA);
	private final Commands commands;
	private final RecipeManager recipes = new RecipeManager();
	private final TagManager tags = new TagManager();
	private final PredicateManager predicateManager = new PredicateManager();
	private final LootTables lootTables = new LootTables(this.predicateManager);
	private final ServerAdvancementManager advancements = new ServerAdvancementManager(this.predicateManager);
	private final ServerFunctionLibrary functionLibrary;

	public ServerResources(boolean bl, int i) {
		this.commands = new Commands(bl);
		this.functionLibrary = new ServerFunctionLibrary(i, this.commands.getDispatcher());
		this.resources.registerReloadListener(this.tags);
		this.resources.registerReloadListener(this.predicateManager);
		this.resources.registerReloadListener(this.recipes);
		this.resources.registerReloadListener(this.lootTables);
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

	public TagManager getTags() {
		return this.tags;
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

	public static CompletableFuture<ServerResources> loadResources(List<Pack> list, boolean bl, int i, Executor executor, Executor executor2) {
		ServerResources serverResources = new ServerResources(bl, i);
		CompletableFuture<Unit> completableFuture = serverResources.resources.reload(executor, executor2, list, DATA_RELOAD_INITIAL_TASK);
		return completableFuture.thenApply(unit -> serverResources);
	}

	public void updateGlobals() {
		this.tags.bindToGlobal();
	}
}
