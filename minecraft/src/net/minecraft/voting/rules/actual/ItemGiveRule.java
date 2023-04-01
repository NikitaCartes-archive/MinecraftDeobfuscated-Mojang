package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.OneShotRule;
import net.minecraft.voting.rules.Rule;
import net.minecraft.voting.rules.RuleChange;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemGiveRule extends OneShotRule.Simple {
	private final Codec<ItemGiveRule.GiveItemRule> codec = ItemStack.CODEC
		.xmap(itemStack -> new ItemGiveRule.GiveItemRule(itemStack), giveItemRule -> giveItemRule.stack);

	@Override
	public Codec<RuleChange> codec() {
		return Rule.puntCodec(this.codec);
	}

	@Override
	public Optional<RuleChange> randomApprovableChange(MinecraftServer minecraftServer, RandomSource randomSource) {
		Optional<Holder.Reference<Item>> optional = minecraftServer.registryAccess().registryOrThrow(Registries.ITEM).getRandom(randomSource);
		return optional.map(reference -> {
			int i = randomSource.nextIntBetweenInclusive(1, ((Item)reference.value()).getMaxStackSize());
			return new ItemGiveRule.GiveItemRule(new ItemStack(reference, i));
		});
	}

	protected class GiveItemRule extends OneShotRule.OneShotRuleChange {
		final ItemStack stack;
		private final Component description;

		protected GiveItemRule(ItemStack itemStack) {
			this.stack = itemStack;
			this.description = Component.translatable("rule.give_items", itemStack.getCount(), itemStack.getDisplayName());
		}

		@Override
		protected Component description() {
			return this.description;
		}

		@Override
		public void run(MinecraftServer minecraftServer) {
			for (ServerPlayer serverPlayer : minecraftServer.getPlayerList().getPlayers()) {
				ItemStack itemStack = this.stack.copy();
				if (serverPlayer.addItem(itemStack)) {
					serverPlayer.level
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
				} else {
					ItemEntity itemEntity = serverPlayer.drop(itemStack, false);
					if (itemEntity != null) {
						itemEntity.setNoPickUpDelay();
						itemEntity.setTarget(serverPlayer.getUUID());
					}
				}

				serverPlayer.containerMenu.broadcastChanges();
			}
		}
	}
}
