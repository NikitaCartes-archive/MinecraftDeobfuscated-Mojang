package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.voting.rules.Rule;
import net.minecraft.voting.rules.RuleAction;
import net.minecraft.voting.rules.RuleChange;

public record ClientboundRuleUpdatePacket(boolean resetAll, RuleAction action, List<RuleChange> rules) implements Packet<ClientGamePacketListener> {
	public ClientboundRuleUpdatePacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readBoolean(), friendlyByteBuf.readEnum(RuleAction.class), friendlyByteBuf.readList(ClientboundRuleUpdatePacket::readChange));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBoolean(this.resetAll);
		friendlyByteBuf.writeEnum(this.action);
		friendlyByteBuf.writeCollection(this.rules, ClientboundRuleUpdatePacket::writeChange);
	}

	private static RuleChange readChange(FriendlyByteBuf friendlyByteBuf) {
		Rule rule = friendlyByteBuf.readById(BuiltInRegistries.RULE);
		return friendlyByteBuf.readWithCodec(NbtOps.INSTANCE, rule.codec());
	}

	private static void writeChange(FriendlyByteBuf friendlyByteBuf, RuleChange ruleChange) {
		friendlyByteBuf.writeId(BuiltInRegistries.RULE, ruleChange.rule());
		friendlyByteBuf.writeWithCodec(NbtOps.INSTANCE, ruleChange.rule().codec(), ruleChange);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleRuleUpdatePacket(this);
	}
}
