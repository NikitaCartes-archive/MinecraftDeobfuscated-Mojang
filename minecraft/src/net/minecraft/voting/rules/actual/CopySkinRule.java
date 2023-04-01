package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.OneShotRule;
import net.minecraft.voting.rules.Rule;
import net.minecraft.voting.rules.RuleChange;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

public class CopySkinRule extends OneShotRule {
	private final Codec<CopySkinRule.CopySkinRuleChange> codec = PlayerEntry.CODEC
		.optionalFieldOf("player")
		.codec()
		.xmap(optional -> new CopySkinRule.CopySkinRuleChange(optional), copySkinRuleChange -> copySkinRuleChange.skin);

	@Override
	public Codec<RuleChange> codec() {
		return Rule.puntCodec(this.codec);
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		boolean bl = minecraftServer.getPlayerList().getPlayers().stream().anyMatch(serverPlayer -> serverPlayer.getTransform().playerSkin() != null);
		ObjectArrayList<Optional<ServerPlayer>> objectArrayList = (ObjectArrayList<Optional<ServerPlayer>>)minecraftServer.getPlayerList()
			.getPlayers()
			.stream()
			.map(Optional::of)
			.collect(Collectors.toCollection(ObjectArrayList::new));
		if (bl) {
			objectArrayList.add(Optional.empty());
		}

		Util.shuffle(objectArrayList, randomSource);
		return objectArrayList.stream().limit((long)i).map(optional -> new CopySkinRule.CopySkinRuleChange(optional.map(PlayerEntry::from)));
	}

	protected class CopySkinRuleChange extends OneShotRule.OneShotRuleChange {
		final Optional<PlayerEntry> skin;
		private final Component description;

		protected CopySkinRuleChange(Optional<PlayerEntry> optional) {
			this.skin = optional;
			this.description = (Component)optional.map(playerEntry -> Component.translatable("rule.copy_skin", playerEntry.displayName()))
				.orElse(Component.translatable("rule.reset_skin"));
		}

		@Override
		protected Component description() {
			return this.description;
		}

		@Override
		public void run(MinecraftServer minecraftServer) {
			if (this.skin.isPresent()) {
				SkullBlockEntity.updateGameprofile(((PlayerEntry)this.skin.get()).profile(), gameProfile -> {
					for (ServerPlayer serverPlayerx : minecraftServer.getPlayerList().getPlayers()) {
						serverPlayerx.updateTransform(entityTransformType -> entityTransformType.withPlayerSkin(Optional.of(gameProfile)));
					}
				});
			} else {
				for (ServerPlayer serverPlayer : minecraftServer.getPlayerList().getPlayers()) {
					serverPlayer.updateTransform(entityTransformType -> entityTransformType.withPlayerSkin(Optional.empty()));
				}
			}
		}
	}
}
