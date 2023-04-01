package net.minecraft.voting.rules;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundRuleUpdatePacket;
import net.minecraft.server.MinecraftServer;

public interface RuleChange {
	Codec<RuleChange> CODEC = BuiltInRegistries.RULE.byNameCodec().dispatch(RuleChange::rule, Rule::codec);

	Rule rule();

	default void apply(RuleAction ruleAction, MinecraftServer minecraftServer) {
		this.update(ruleAction);
		minecraftServer.getPlayerList().broadcastAll(new ClientboundRuleUpdatePacket(false, ruleAction, List.of(this)));
	}

	void update(RuleAction ruleAction);

	Component description(RuleAction ruleAction);

	public interface Simple extends RuleChange {
		Component description();

		@Override
		default Component description(RuleAction ruleAction) {
			return (Component)(switch (ruleAction) {
				case REPEAL -> Component.translatable("action.repeal", this.description());
				case APPROVE -> this.description();
			});
		}
	}
}
