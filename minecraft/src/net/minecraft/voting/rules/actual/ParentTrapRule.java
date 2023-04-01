package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.OneShotRule;
import net.minecraft.voting.rules.Rule;
import net.minecraft.voting.rules.RuleChange;

public class ParentTrapRule extends OneShotRule {
	private final Codec<ParentTrapRule.ParentTrapRuleChange> codec = Codec.unit(new ParentTrapRule.ParentTrapRuleChange());

	@Override
	public Codec<RuleChange> codec() {
		return Rule.puntCodec(this.codec);
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		return minecraftServer.getPlayerList().getPlayers().size() > 1 ? Stream.of(new ParentTrapRule.ParentTrapRuleChange()) : Stream.empty();
	}

	protected class ParentTrapRuleChange extends OneShotRule.OneShotRuleChange {
		private final Component description = Component.translatable("rule.parent_trap");

		@Override
		protected Component description() {
			return this.description;
		}

		@Override
		public void run(MinecraftServer minecraftServer) {
			ObjectArrayList<ServerPlayer> objectArrayList = new ObjectArrayList<>(minecraftServer.getPlayerList().getPlayers());
			Util.shuffle(objectArrayList, minecraftServer.overworld().random);
			double d = objectArrayList.get(0).getX();
			double e = objectArrayList.get(0).getY();
			double f = objectArrayList.get(0).getZ();
			ServerLevel serverLevel = objectArrayList.get(0).getLevel();
			float g = objectArrayList.get(0).getYRot();
			float h = objectArrayList.get(0).getXRot();

			for (int i = 0; i < objectArrayList.size(); i++) {
				ServerPlayer serverPlayer = objectArrayList.get(i);
				if (i == objectArrayList.size() - 1) {
					serverPlayer.teleportTo(serverLevel, d, e, f, g, h);
				} else {
					ServerPlayer serverPlayer2 = objectArrayList.get(i + 1);
					serverPlayer.teleportTo(
						serverPlayer2.getLevel(), serverPlayer2.getX(), serverPlayer2.getY(), serverPlayer2.getZ(), serverPlayer2.getYRot(), serverPlayer2.getXRot()
					);
				}
			}
		}
	}
}
