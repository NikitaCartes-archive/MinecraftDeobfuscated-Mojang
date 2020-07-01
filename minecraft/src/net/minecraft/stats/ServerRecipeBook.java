package net.minecraft.stats;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerRecipeBook extends RecipeBook {
	private static final Logger LOGGER = LogManager.getLogger();

	public int addRecipes(Collection<Recipe<?>> collection, ServerPlayer serverPlayer) {
		List<ResourceLocation> list = Lists.<ResourceLocation>newArrayList();
		int i = 0;

		for (Recipe<?> recipe : collection) {
			ResourceLocation resourceLocation = recipe.getId();
			if (!this.known.contains(resourceLocation) && !recipe.isSpecial()) {
				this.add(resourceLocation);
				this.addHighlight(resourceLocation);
				list.add(resourceLocation);
				CriteriaTriggers.RECIPE_UNLOCKED.trigger(serverPlayer, recipe);
				i++;
			}
		}

		this.sendRecipes(ClientboundRecipePacket.State.ADD, serverPlayer, list);
		return i;
	}

	public int removeRecipes(Collection<Recipe<?>> collection, ServerPlayer serverPlayer) {
		List<ResourceLocation> list = Lists.<ResourceLocation>newArrayList();
		int i = 0;

		for (Recipe<?> recipe : collection) {
			ResourceLocation resourceLocation = recipe.getId();
			if (this.known.contains(resourceLocation)) {
				this.remove(resourceLocation);
				list.add(resourceLocation);
				i++;
			}
		}

		this.sendRecipes(ClientboundRecipePacket.State.REMOVE, serverPlayer, list);
		return i;
	}

	private void sendRecipes(ClientboundRecipePacket.State state, ServerPlayer serverPlayer, List<ResourceLocation> list) {
		serverPlayer.connection.send(new ClientboundRecipePacket(state, list, Collections.emptyList(), this.getBookSettings()));
	}

	public CompoundTag toNbt() {
		CompoundTag compoundTag = new CompoundTag();
		this.getBookSettings().write(compoundTag);
		ListTag listTag = new ListTag();

		for (ResourceLocation resourceLocation : this.known) {
			listTag.add(StringTag.valueOf(resourceLocation.toString()));
		}

		compoundTag.put("recipes", listTag);
		ListTag listTag2 = new ListTag();

		for (ResourceLocation resourceLocation2 : this.highlight) {
			listTag2.add(StringTag.valueOf(resourceLocation2.toString()));
		}

		compoundTag.put("toBeDisplayed", listTag2);
		return compoundTag;
	}

	public void fromNbt(CompoundTag compoundTag, RecipeManager recipeManager) {
		this.setBookSettings(RecipeBookSettings.read(compoundTag));
		ListTag listTag = compoundTag.getList("recipes", 8);
		this.loadRecipes(listTag, this::add, recipeManager);
		ListTag listTag2 = compoundTag.getList("toBeDisplayed", 8);
		this.loadRecipes(listTag2, this::addHighlight, recipeManager);
	}

	private void loadRecipes(ListTag listTag, Consumer<Recipe<?>> consumer, RecipeManager recipeManager) {
		for (int i = 0; i < listTag.size(); i++) {
			String string = listTag.getString(i);

			try {
				ResourceLocation resourceLocation = new ResourceLocation(string);
				Optional<? extends Recipe<?>> optional = recipeManager.byKey(resourceLocation);
				if (!optional.isPresent()) {
					LOGGER.error("Tried to load unrecognized recipe: {} removed now.", resourceLocation);
				} else {
					consumer.accept(optional.get());
				}
			} catch (ResourceLocationException var8) {
				LOGGER.error("Tried to load improperly formatted recipe: {} removed now.", string);
			}
		}
	}

	public void sendInitialRecipeBook(ServerPlayer serverPlayer) {
		serverPlayer.connection.send(new ClientboundRecipePacket(ClientboundRecipePacket.State.INIT, this.known, this.highlight, this.getBookSettings()));
	}
}
