package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundCommandsPacket implements Packet<ClientGamePacketListener> {
	private RootCommandNode<SharedSuggestionProvider> root;

	public ClientboundCommandsPacket() {
	}

	public ClientboundCommandsPacket(RootCommandNode<SharedSuggestionProvider> rootCommandNode) {
		this.root = rootCommandNode;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		ClientboundCommandsPacket.Entry[] entrys = new ClientboundCommandsPacket.Entry[friendlyByteBuf.readVarInt()];

		for (int i = 0; i < entrys.length; i++) {
			entrys[i] = readNode(friendlyByteBuf);
		}

		resolveEntries(entrys);
		this.root = (RootCommandNode<SharedSuggestionProvider>)entrys[friendlyByteBuf.readVarInt()].node;
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		Object2IntMap<CommandNode<SharedSuggestionProvider>> object2IntMap = enumerateNodes(this.root);
		CommandNode<SharedSuggestionProvider>[] commandNodes = getNodesInIdOrder(object2IntMap);
		friendlyByteBuf.writeVarInt(commandNodes.length);

		for (CommandNode<SharedSuggestionProvider> commandNode : commandNodes) {
			writeNode(friendlyByteBuf, commandNode, object2IntMap);
		}

		friendlyByteBuf.writeVarInt(object2IntMap.get(this.root));
	}

	private static void resolveEntries(ClientboundCommandsPacket.Entry[] entrys) {
		List<ClientboundCommandsPacket.Entry> list = Lists.<ClientboundCommandsPacket.Entry>newArrayList(entrys);

		while (!list.isEmpty()) {
			boolean bl = list.removeIf(entry -> entry.build(entrys));
			if (!bl) {
				throw new IllegalStateException("Server sent an impossible command tree");
			}
		}
	}

	private static Object2IntMap<CommandNode<SharedSuggestionProvider>> enumerateNodes(RootCommandNode<SharedSuggestionProvider> rootCommandNode) {
		Object2IntMap<CommandNode<SharedSuggestionProvider>> object2IntMap = new Object2IntOpenHashMap<>();
		Queue<CommandNode<SharedSuggestionProvider>> queue = Queues.<CommandNode<SharedSuggestionProvider>>newArrayDeque();
		queue.add(rootCommandNode);

		CommandNode<SharedSuggestionProvider> commandNode;
		while ((commandNode = (CommandNode<SharedSuggestionProvider>)queue.poll()) != null) {
			if (!object2IntMap.containsKey(commandNode)) {
				int i = object2IntMap.size();
				object2IntMap.put(commandNode, i);
				queue.addAll(commandNode.getChildren());
				if (commandNode.getRedirect() != null) {
					queue.add(commandNode.getRedirect());
				}
			}
		}

		return object2IntMap;
	}

	private static CommandNode<SharedSuggestionProvider>[] getNodesInIdOrder(Object2IntMap<CommandNode<SharedSuggestionProvider>> object2IntMap) {
		CommandNode<SharedSuggestionProvider>[] commandNodes = new CommandNode[object2IntMap.size()];

		for (Object2IntMap.Entry<CommandNode<SharedSuggestionProvider>> entry : Object2IntMaps.fastIterable(object2IntMap)) {
			commandNodes[entry.getIntValue()] = (CommandNode<SharedSuggestionProvider>)entry.getKey();
		}

		return commandNodes;
	}

	private static ClientboundCommandsPacket.Entry readNode(FriendlyByteBuf friendlyByteBuf) {
		byte b = friendlyByteBuf.readByte();
		int[] is = friendlyByteBuf.readVarIntArray();
		int i = (b & 8) != 0 ? friendlyByteBuf.readVarInt() : 0;
		ArgumentBuilder<SharedSuggestionProvider, ?> argumentBuilder = createBuilder(friendlyByteBuf, b);
		return new ClientboundCommandsPacket.Entry(argumentBuilder, b, i, is);
	}

	@Nullable
	private static ArgumentBuilder<SharedSuggestionProvider, ?> createBuilder(FriendlyByteBuf friendlyByteBuf, byte b) {
		int i = b & 3;
		if (i == 2) {
			String string = friendlyByteBuf.readUtf(32767);
			ArgumentType<?> argumentType = ArgumentTypes.deserialize(friendlyByteBuf);
			if (argumentType == null) {
				return null;
			} else {
				RequiredArgumentBuilder<SharedSuggestionProvider, ?> requiredArgumentBuilder = RequiredArgumentBuilder.argument(string, argumentType);
				if ((b & 16) != 0) {
					requiredArgumentBuilder.suggests(SuggestionProviders.getProvider(friendlyByteBuf.readResourceLocation()));
				}

				return requiredArgumentBuilder;
			}
		} else {
			return i == 1 ? LiteralArgumentBuilder.literal(friendlyByteBuf.readUtf(32767)) : null;
		}
	}

	private static void writeNode(
		FriendlyByteBuf friendlyByteBuf, CommandNode<SharedSuggestionProvider> commandNode, Map<CommandNode<SharedSuggestionProvider>, Integer> map
	) {
		byte b = 0;
		if (commandNode.getRedirect() != null) {
			b = (byte)(b | 8);
		}

		if (commandNode.getCommand() != null) {
			b = (byte)(b | 4);
		}

		if (commandNode instanceof RootCommandNode) {
			b = (byte)(b | 0);
		} else if (commandNode instanceof ArgumentCommandNode) {
			b = (byte)(b | 2);
			if (((ArgumentCommandNode)commandNode).getCustomSuggestions() != null) {
				b = (byte)(b | 16);
			}
		} else {
			if (!(commandNode instanceof LiteralCommandNode)) {
				throw new UnsupportedOperationException("Unknown node type " + commandNode);
			}

			b = (byte)(b | 1);
		}

		friendlyByteBuf.writeByte(b);
		friendlyByteBuf.writeVarInt(commandNode.getChildren().size());

		for (CommandNode<SharedSuggestionProvider> commandNode2 : commandNode.getChildren()) {
			friendlyByteBuf.writeVarInt((Integer)map.get(commandNode2));
		}

		if (commandNode.getRedirect() != null) {
			friendlyByteBuf.writeVarInt((Integer)map.get(commandNode.getRedirect()));
		}

		if (commandNode instanceof ArgumentCommandNode) {
			ArgumentCommandNode<SharedSuggestionProvider, ?> argumentCommandNode = (ArgumentCommandNode<SharedSuggestionProvider, ?>)commandNode;
			friendlyByteBuf.writeUtf(argumentCommandNode.getName());
			ArgumentTypes.serialize(friendlyByteBuf, argumentCommandNode.getType());
			if (argumentCommandNode.getCustomSuggestions() != null) {
				friendlyByteBuf.writeResourceLocation(SuggestionProviders.getName(argumentCommandNode.getCustomSuggestions()));
			}
		} else if (commandNode instanceof LiteralCommandNode) {
			friendlyByteBuf.writeUtf(((LiteralCommandNode)commandNode).getLiteral());
		}
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleCommands(this);
	}

	@Environment(EnvType.CLIENT)
	public RootCommandNode<SharedSuggestionProvider> getRoot() {
		return this.root;
	}

	static class Entry {
		@Nullable
		private final ArgumentBuilder<SharedSuggestionProvider, ?> builder;
		private final byte flags;
		private final int redirect;
		private final int[] children;
		@Nullable
		private CommandNode<SharedSuggestionProvider> node;

		private Entry(@Nullable ArgumentBuilder<SharedSuggestionProvider, ?> argumentBuilder, byte b, int i, int[] is) {
			this.builder = argumentBuilder;
			this.flags = b;
			this.redirect = i;
			this.children = is;
		}

		public boolean build(ClientboundCommandsPacket.Entry[] entrys) {
			if (this.node == null) {
				if (this.builder == null) {
					this.node = new RootCommandNode<>();
				} else {
					if ((this.flags & 8) != 0) {
						if (entrys[this.redirect].node == null) {
							return false;
						}

						this.builder.redirect(entrys[this.redirect].node);
					}

					if ((this.flags & 4) != 0) {
						this.builder.executes(commandContext -> 0);
					}

					this.node = this.builder.build();
				}
			}

			for (int i : this.children) {
				if (entrys[i].node == null) {
					return false;
				}
			}

			for (int ix : this.children) {
				CommandNode<SharedSuggestionProvider> commandNode = entrys[ix].node;
				if (!(commandNode instanceof RootCommandNode)) {
					this.node.addChild(commandNode);
				}
			}

			return true;
		}
	}
}
