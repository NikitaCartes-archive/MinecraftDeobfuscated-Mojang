package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.OneShotRule;
import net.minecraft.voting.rules.Rule;
import net.minecraft.voting.rules.RuleChange;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ReplaceItemsRule extends OneShotRule.Simple {
	private final Codec<ReplaceItemsRule.ReplaceRuleChange> codec = RecordCodecBuilder.create(
		instance -> instance.group(
					BuiltInRegistries.ITEM.byNameCodec().fieldOf("source").forGetter(replaceRuleChange -> replaceRuleChange.source),
					BuiltInRegistries.ITEM.byNameCodec().fieldOf("target").forGetter(replaceRuleChange -> replaceRuleChange.target)
				)
				.apply(instance, (item, item2) -> new ReplaceItemsRule.ReplaceRuleChange(item, item2))
	);
	private final ReplaceItemsRule.ItemGenerator generator;

	public ReplaceItemsRule(ReplaceItemsRule.ItemGenerator itemGenerator) {
		this.generator = itemGenerator;
	}

	@Override
	public Codec<RuleChange> codec() {
		return Rule.puntCodec(this.codec);
	}

	@Override
	protected Optional<RuleChange> randomApprovableChange(MinecraftServer minecraftServer, RandomSource randomSource) {
		Registry<Item> registry = minecraftServer.registryAccess().registryOrThrow(Registries.ITEM);
		Optional<Item> optional = pickSourceItem(minecraftServer, randomSource, registry);
		Optional<Item> optional2 = this.generator.get(registry, randomSource);
		return optional.isPresent() && optional2.isPresent() && !optional.equals(optional2)
			? Optional.of(new ReplaceItemsRule.ReplaceRuleChange((Item)optional.get(), (Item)optional2.get()))
			: Optional.empty();
	}

	private static Optional<Item> pickSourceItem(MinecraftServer minecraftServer, RandomSource randomSource, Registry<Item> registry) {
		if (randomSource.nextInt(10) != 0) {
			List<Item> list = registry.stream()
				.filter(
					item -> {
						for (ServerPlayer serverPlayer : minecraftServer.getPlayerList().getPlayers()) {
							ServerStatsCounter serverStatsCounter = serverPlayer.getStats();
							if (serverStatsCounter.getValue(Stats.ITEM_PICKED_UP, item) > 0
								|| serverStatsCounter.getValue(Stats.ITEM_USED, item) > 0
								|| serverStatsCounter.getValue(Stats.ITEM_CRAFTED, item) > 0) {
								return true;
							}
						}

						return false;
					}
				)
				.toList();
			if (!list.isEmpty()) {
				return Util.getRandomSafe(list, randomSource);
			}
		}

		return registry.getRandom(randomSource).map(Holder.Reference::value).filter(item -> item != Items.AIR);
	}

	public interface ItemGenerator {
		Optional<Item> get(Registry<Item> registry, RandomSource randomSource);
	}

	protected class ReplaceRuleChange extends OneShotRule.OneShotRuleChange {
		final Item source;
		final Item target;
		private final Component description;

		protected ReplaceRuleChange(Item item, Item item2) {
			this.source = item;
			this.target = item2;
			this.description = Component.translatable("rule.replace_items", item.getDescription(), item2.getDescription());
		}

		@Override
		protected Component description() {
			return this.description;
		}

		@Override
		public void run(MinecraftServer minecraftServer) {
			for (ServerPlayer serverPlayer : minecraftServer.getPlayerList().getPlayers()) {
				serverPlayer.getInventory().replaceAll(this.source, this.target);
				serverPlayer.containerMenu.broadcastChanges();
			}
		}
	}
}
