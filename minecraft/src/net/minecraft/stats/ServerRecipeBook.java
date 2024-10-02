package net.minecraft.stats;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ClientboundRecipeBookAddPacket;
import net.minecraft.network.protocol.game.ClientboundRecipeBookRemovePacket;
import net.minecraft.network.protocol.game.ClientboundRecipeBookSettingsPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import org.slf4j.Logger;

public class ServerRecipeBook extends RecipeBook {
	public static final String RECIPE_BOOK_TAG = "recipeBook";
	private static final Logger LOGGER = LogUtils.getLogger();
	private final ServerRecipeBook.DisplayResolver displayResolver;
	@VisibleForTesting
	protected final Set<ResourceKey<Recipe<?>>> known = Sets.newIdentityHashSet();
	@VisibleForTesting
	protected final Set<ResourceKey<Recipe<?>>> highlight = Sets.newIdentityHashSet();

	public ServerRecipeBook(ServerRecipeBook.DisplayResolver displayResolver) {
		this.displayResolver = displayResolver;
	}

	public void add(ResourceKey<Recipe<?>> resourceKey) {
		this.known.add(resourceKey);
	}

	public boolean contains(ResourceKey<Recipe<?>> resourceKey) {
		return this.known.contains(resourceKey);
	}

	public void remove(ResourceKey<Recipe<?>> resourceKey) {
		this.known.remove(resourceKey);
		this.highlight.remove(resourceKey);
	}

	public void removeHighlight(ResourceKey<Recipe<?>> resourceKey) {
		this.highlight.remove(resourceKey);
	}

	private void addHighlight(ResourceKey<Recipe<?>> resourceKey) {
		this.highlight.add(resourceKey);
	}

	public int addRecipes(Collection<RecipeHolder<?>> collection, ServerPlayer serverPlayer) {
		List<ClientboundRecipeBookAddPacket.Entry> list = new ArrayList();

		for (RecipeHolder<?> recipeHolder : collection) {
			ResourceKey<Recipe<?>> resourceKey = recipeHolder.id();
			if (!this.known.contains(resourceKey) && !recipeHolder.value().isSpecial()) {
				this.add(resourceKey);
				this.addHighlight(resourceKey);
				this.displayResolver
					.displaysForRecipe(
						resourceKey, recipeDisplayEntry -> list.add(new ClientboundRecipeBookAddPacket.Entry(recipeDisplayEntry, recipeHolder.value().showNotification(), true))
					);
				CriteriaTriggers.RECIPE_UNLOCKED.trigger(serverPlayer, recipeHolder);
			}
		}

		if (!list.isEmpty()) {
			serverPlayer.connection.send(new ClientboundRecipeBookAddPacket(list));
		}

		return list.size();
	}

	public int removeRecipes(Collection<RecipeHolder<?>> collection, ServerPlayer serverPlayer) {
		List<RecipeDisplayId> list = Lists.<RecipeDisplayId>newArrayList();

		for (RecipeHolder<?> recipeHolder : collection) {
			ResourceKey<Recipe<?>> resourceKey = recipeHolder.id();
			if (this.known.contains(resourceKey)) {
				this.remove(resourceKey);
				this.displayResolver.displaysForRecipe(resourceKey, recipeDisplayEntry -> list.add(recipeDisplayEntry.id()));
			}
		}

		if (!list.isEmpty()) {
			serverPlayer.connection.send(new ClientboundRecipeBookRemovePacket(list));
		}

		return list.size();
	}

	public CompoundTag toNbt() {
		CompoundTag compoundTag = new CompoundTag();
		this.getBookSettings().write(compoundTag);
		ListTag listTag = new ListTag();

		for (ResourceKey<Recipe<?>> resourceKey : this.known) {
			listTag.add(StringTag.valueOf(resourceKey.location().toString()));
		}

		compoundTag.put("recipes", listTag);
		ListTag listTag2 = new ListTag();

		for (ResourceKey<Recipe<?>> resourceKey2 : this.highlight) {
			listTag2.add(StringTag.valueOf(resourceKey2.location().toString()));
		}

		compoundTag.put("toBeDisplayed", listTag2);
		return compoundTag;
	}

	public void fromNbt(CompoundTag compoundTag, Predicate<ResourceKey<Recipe<?>>> predicate) {
		this.setBookSettings(RecipeBookSettings.read(compoundTag));
		ListTag listTag = compoundTag.getList("recipes", 8);
		this.loadRecipes(listTag, this::add, predicate);
		ListTag listTag2 = compoundTag.getList("toBeDisplayed", 8);
		this.loadRecipes(listTag2, this::addHighlight, predicate);
	}

	private void loadRecipes(ListTag listTag, Consumer<ResourceKey<Recipe<?>>> consumer, Predicate<ResourceKey<Recipe<?>>> predicate) {
		for (int i = 0; i < listTag.size(); i++) {
			String string = listTag.getString(i);

			try {
				ResourceKey<Recipe<?>> resourceKey = ResourceKey.create(Registries.RECIPE, ResourceLocation.parse(string));
				if (!predicate.test(resourceKey)) {
					LOGGER.error("Tried to load unrecognized recipe: {} removed now.", resourceKey);
				} else {
					consumer.accept(resourceKey);
				}
			} catch (ResourceLocationException var7) {
				LOGGER.error("Tried to load improperly formatted recipe: {} removed now.", string);
			}
		}
	}

	public void sendInitialRecipeBook(ServerPlayer serverPlayer) {
		serverPlayer.connection.send(new ClientboundRecipeBookSettingsPacket(this.getBookSettings()));
		List<ClientboundRecipeBookAddPacket.Entry> list = new ArrayList(this.known.size());

		for (ResourceKey<Recipe<?>> resourceKey : this.known) {
			this.displayResolver
				.displaysForRecipe(
					resourceKey, recipeDisplayEntry -> list.add(new ClientboundRecipeBookAddPacket.Entry(recipeDisplayEntry, false, this.highlight.contains(resourceKey)))
				);
		}

		serverPlayer.connection.send(new ClientboundRecipeBookAddPacket(list));
	}

	public void copyOverData(ServerRecipeBook serverRecipeBook) {
		this.known.clear();
		this.highlight.clear();
		this.bookSettings.replaceFrom(serverRecipeBook.bookSettings);
		this.known.addAll(serverRecipeBook.known);
		this.highlight.addAll(serverRecipeBook.highlight);
	}

	@FunctionalInterface
	public interface DisplayResolver {
		void displaysForRecipe(ResourceKey<Recipe<?>> resourceKey, Consumer<RecipeDisplayEntry> consumer);
	}
}
