package net.minecraft.advancements;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CacheableFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record AdvancementRewards(int experience, List<ResourceKey<LootTable>> loot, List<ResourceLocation> recipes, Optional<CacheableFunction> function) {
	public static final Codec<AdvancementRewards> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.strictOptionalField(Codec.INT, "experience", 0).forGetter(AdvancementRewards::experience),
					ExtraCodecs.strictOptionalField(ResourceKey.codec(Registries.LOOT_TABLE).listOf(), "loot", List.of()).forGetter(AdvancementRewards::loot),
					ExtraCodecs.strictOptionalField(ResourceLocation.CODEC.listOf(), "recipes", List.of()).forGetter(AdvancementRewards::recipes),
					ExtraCodecs.strictOptionalField(CacheableFunction.CODEC, "function").forGetter(AdvancementRewards::function)
				)
				.apply(instance, AdvancementRewards::new)
	);
	public static final AdvancementRewards EMPTY = new AdvancementRewards(0, List.of(), List.of(), Optional.empty());

	public void grant(ServerPlayer serverPlayer) {
		serverPlayer.giveExperiencePoints(this.experience);
		LootParams lootParams = new LootParams.Builder(serverPlayer.serverLevel())
			.withParameter(LootContextParams.THIS_ENTITY, serverPlayer)
			.withParameter(LootContextParams.ORIGIN, serverPlayer.position())
			.create(LootContextParamSets.ADVANCEMENT_REWARD);
		boolean bl = false;

		for (ResourceKey<LootTable> resourceKey : this.loot) {
			for (ItemStack itemStack : serverPlayer.server.reloadableRegistries().getLootTable(resourceKey).getRandomItems(lootParams)) {
				if (serverPlayer.addItem(itemStack)) {
					serverPlayer.level()
						.playSound(
							null,
							serverPlayer.getX(),
							serverPlayer.getY(),
							serverPlayer.getZ(),
							SoundEvents.ITEM_PICKUP,
							SoundSource.PLAYERS,
							0.2F,
							((serverPlayer.getRandom().nextFloat() - serverPlayer.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
						);
					bl = true;
				} else {
					ItemEntity itemEntity = serverPlayer.drop(itemStack, false);
					if (itemEntity != null) {
						itemEntity.setNoPickUpDelay();
						itemEntity.setTarget(serverPlayer.getUUID());
					}
				}
			}
		}

		if (bl) {
			serverPlayer.containerMenu.broadcastChanges();
		}

		if (!this.recipes.isEmpty()) {
			serverPlayer.awardRecipesByKey(this.recipes);
		}

		MinecraftServer minecraftServer = serverPlayer.server;
		this.function
			.flatMap(cacheableFunction -> cacheableFunction.get(minecraftServer.getFunctions()))
			.ifPresent(
				commandFunction -> minecraftServer.getFunctions()
						.execute(commandFunction, serverPlayer.createCommandSourceStack().withSuppressedOutput().withPermission(2))
			);
	}

	public static class Builder {
		private int experience;
		private final ImmutableList.Builder<ResourceKey<LootTable>> loot = ImmutableList.builder();
		private final ImmutableList.Builder<ResourceLocation> recipes = ImmutableList.builder();
		private Optional<ResourceLocation> function = Optional.empty();

		public static AdvancementRewards.Builder experience(int i) {
			return new AdvancementRewards.Builder().addExperience(i);
		}

		public AdvancementRewards.Builder addExperience(int i) {
			this.experience += i;
			return this;
		}

		public static AdvancementRewards.Builder loot(ResourceKey<LootTable> resourceKey) {
			return new AdvancementRewards.Builder().addLootTable(resourceKey);
		}

		public AdvancementRewards.Builder addLootTable(ResourceKey<LootTable> resourceKey) {
			this.loot.add(resourceKey);
			return this;
		}

		public static AdvancementRewards.Builder recipe(ResourceLocation resourceLocation) {
			return new AdvancementRewards.Builder().addRecipe(resourceLocation);
		}

		public AdvancementRewards.Builder addRecipe(ResourceLocation resourceLocation) {
			this.recipes.add(resourceLocation);
			return this;
		}

		public static AdvancementRewards.Builder function(ResourceLocation resourceLocation) {
			return new AdvancementRewards.Builder().runs(resourceLocation);
		}

		public AdvancementRewards.Builder runs(ResourceLocation resourceLocation) {
			this.function = Optional.of(resourceLocation);
			return this;
		}

		public AdvancementRewards build() {
			return new AdvancementRewards(this.experience, this.loot.build(), this.recipes.build(), this.function.map(CacheableFunction::new));
		}
	}
}
