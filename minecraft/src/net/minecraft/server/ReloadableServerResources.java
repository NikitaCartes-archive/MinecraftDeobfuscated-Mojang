package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.util.Unit;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.slf4j.Logger;

public class ReloadableServerResources {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final CompletableFuture<Unit> DATA_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
	private final ReloadableServerResources.ConfigurableRegistryLookup registryLookup;
	private final Commands commands;
	private final RecipeManager recipes;
	private final TagManager tagManager;
	private final LootDataManager lootData;
	private final ServerAdvancementManager advancements;
	private final ServerFunctionLibrary functionLibrary;

	public ReloadableServerResources(RegistryAccess.Frozen frozen, FeatureFlagSet featureFlagSet, Commands.CommandSelection commandSelection, int i) {
		this.registryLookup = new ReloadableServerResources.ConfigurableRegistryLookup(frozen);
		this.registryLookup.missingTagAccessPolicy(ReloadableServerResources.MissingTagAccessPolicy.CREATE_NEW);
		this.recipes = new RecipeManager(this.registryLookup);
		this.tagManager = new TagManager(frozen);
		this.commands = new Commands(commandSelection, CommandBuildContext.simple(this.registryLookup, featureFlagSet));
		this.lootData = new LootDataManager(this.registryLookup);
		this.advancements = new ServerAdvancementManager(this.registryLookup, this.lootData);
		this.functionLibrary = new ServerFunctionLibrary(i, this.commands.getDispatcher());
	}

	public ServerFunctionLibrary getFunctionLibrary() {
		return this.functionLibrary;
	}

	public LootDataManager getLootData() {
		return this.lootData;
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
		return List.of(this.tagManager, this.lootData, this.recipes, this.functionLibrary, this.advancements);
	}

	public static CompletableFuture<ReloadableServerResources> loadResources(
		ResourceManager resourceManager,
		RegistryAccess.Frozen frozen,
		FeatureFlagSet featureFlagSet,
		Commands.CommandSelection commandSelection,
		int i,
		Executor executor,
		Executor executor2
	) {
		ReloadableServerResources reloadableServerResources = new ReloadableServerResources(frozen, featureFlagSet, commandSelection, i);
		return SimpleReloadInstance.create(
				resourceManager, reloadableServerResources.listeners(), executor, executor2, DATA_RELOAD_INITIAL_TASK, LOGGER.isDebugEnabled()
			)
			.done()
			.whenComplete((object, throwable) -> reloadableServerResources.registryLookup.missingTagAccessPolicy(ReloadableServerResources.MissingTagAccessPolicy.FAIL))
			.thenApply(object -> reloadableServerResources);
	}

	public void updateRegistryTags(RegistryAccess registryAccess) {
		this.tagManager.getResult().forEach(loadResult -> updateRegistryTags(registryAccess, loadResult));
		AbstractFurnaceBlockEntity.invalidateCache();
		Blocks.rebuildCache();
	}

	private static <T> void updateRegistryTags(RegistryAccess registryAccess, TagManager.LoadResult<T> loadResult) {
		ResourceKey<? extends Registry<T>> resourceKey = loadResult.key();
		Map<TagKey<T>, List<Holder<T>>> map = (Map<TagKey<T>, List<Holder<T>>>)loadResult.tags()
			.entrySet()
			.stream()
			.collect(
				Collectors.toUnmodifiableMap(entry -> TagKey.create(resourceKey, (ResourceLocation)entry.getKey()), entry -> List.copyOf((Collection)entry.getValue()))
			);
		registryAccess.registryOrThrow(resourceKey).bindTags(map);
	}

	static class ConfigurableRegistryLookup implements HolderLookup.Provider {
		private final RegistryAccess registryAccess;
		ReloadableServerResources.MissingTagAccessPolicy missingTagAccessPolicy = ReloadableServerResources.MissingTagAccessPolicy.FAIL;

		ConfigurableRegistryLookup(RegistryAccess registryAccess) {
			this.registryAccess = registryAccess;
		}

		public void missingTagAccessPolicy(ReloadableServerResources.MissingTagAccessPolicy missingTagAccessPolicy) {
			this.missingTagAccessPolicy = missingTagAccessPolicy;
		}

		@Override
		public Stream<ResourceKey<? extends Registry<?>>> listRegistries() {
			return this.registryAccess.listRegistries();
		}

		@Override
		public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
			return this.registryAccess.registry(resourceKey).map(registry -> this.createDispatchedLookup(registry.asLookup(), registry.asTagAddingLookup()));
		}

		private <T> HolderLookup.RegistryLookup<T> createDispatchedLookup(
			HolderLookup.RegistryLookup<T> registryLookup, HolderLookup.RegistryLookup<T> registryLookup2
		) {
			return new HolderLookup.RegistryLookup.Delegate<T>() {
				@Override
				public HolderLookup.RegistryLookup<T> parent() {
					return switch (ConfigurableRegistryLookup.this.missingTagAccessPolicy) {
						case FAIL -> registryLookup;
						case CREATE_NEW -> registryLookup2;
					};
				}
			};
		}
	}

	static enum MissingTagAccessPolicy {
		CREATE_NEW,
		FAIL;
	}
}
