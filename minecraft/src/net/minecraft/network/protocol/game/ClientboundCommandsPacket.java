package net.minecraft.network.protocol.game;

import com.google.common.collect.Queues;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiPredicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundCommandsPacket implements Packet<ClientGamePacketListener> {
	private static final byte MASK_TYPE = 3;
	private static final byte FLAG_EXECUTABLE = 4;
	private static final byte FLAG_REDIRECT = 8;
	private static final byte FLAG_CUSTOM_SUGGESTIONS = 16;
	private static final byte TYPE_ROOT = 0;
	private static final byte TYPE_LITERAL = 1;
	private static final byte TYPE_ARGUMENT = 2;
	private final int rootIndex;
	private final List<ClientboundCommandsPacket.Entry> entries;

	public ClientboundCommandsPacket(RootCommandNode<SharedSuggestionProvider> rootCommandNode) {
		Object2IntMap<CommandNode<SharedSuggestionProvider>> object2IntMap = enumerateNodes(rootCommandNode);
		this.entries = createEntries(object2IntMap);
		this.rootIndex = object2IntMap.getInt(rootCommandNode);
	}

	public ClientboundCommandsPacket(FriendlyByteBuf friendlyByteBuf) {
		this.entries = friendlyByteBuf.readList(ClientboundCommandsPacket::readNode);
		this.rootIndex = friendlyByteBuf.readVarInt();
		validateEntries(this.entries);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeCollection(this.entries, (friendlyByteBufx, entry) -> entry.write(friendlyByteBufx));
		friendlyByteBuf.writeVarInt(this.rootIndex);
	}

	private static void validateEntries(List<ClientboundCommandsPacket.Entry> list, BiPredicate<ClientboundCommandsPacket.Entry, IntSet> biPredicate) {
		IntSet intSet = new IntOpenHashSet(IntSets.fromTo(0, list.size()));

		while (!intSet.isEmpty()) {
			boolean bl = intSet.removeIf(i -> biPredicate.test((ClientboundCommandsPacket.Entry)list.get(i), intSet));
			if (!bl) {
				throw new IllegalStateException("Server sent an impossible command tree");
			}
		}
	}

	private static void validateEntries(List<ClientboundCommandsPacket.Entry> list) {
		validateEntries(list, ClientboundCommandsPacket.Entry::canBuild);
		validateEntries(list, ClientboundCommandsPacket.Entry::canResolve);
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

	private static List<ClientboundCommandsPacket.Entry> createEntries(Object2IntMap<CommandNode<SharedSuggestionProvider>> object2IntMap) {
		ObjectArrayList<ClientboundCommandsPacket.Entry> objectArrayList = new ObjectArrayList<>(object2IntMap.size());
		objectArrayList.size(object2IntMap.size());

		for (Object2IntMap.Entry<CommandNode<SharedSuggestionProvider>> entry : Object2IntMaps.fastIterable(object2IntMap)) {
			objectArrayList.set(entry.getIntValue(), createEntry((CommandNode<SharedSuggestionProvider>)entry.getKey(), object2IntMap));
		}

		return objectArrayList;
	}

	private static ClientboundCommandsPacket.Entry readNode(FriendlyByteBuf friendlyByteBuf) {
		byte b = friendlyByteBuf.readByte();
		int[] is = friendlyByteBuf.readVarIntArray();
		int i = (b & 8) != 0 ? friendlyByteBuf.readVarInt() : 0;
		ClientboundCommandsPacket.NodeStub nodeStub = read(friendlyByteBuf, b);
		return new ClientboundCommandsPacket.Entry(nodeStub, b, i, is);
	}

	@Nullable
	private static ClientboundCommandsPacket.NodeStub read(FriendlyByteBuf friendlyByteBuf, byte b) {
		int i = b & 3;
		if (i == 2) {
			String string = friendlyByteBuf.readUtf();
			int j = friendlyByteBuf.readVarInt();
			ArgumentTypeInfo<?, ?> argumentTypeInfo = Registry.COMMAND_ARGUMENT_TYPE.byId(j);
			if (argumentTypeInfo == null) {
				return null;
			} else {
				ArgumentTypeInfo.Template<?> template = argumentTypeInfo.deserializeFromNetwork(friendlyByteBuf);
				ResourceLocation resourceLocation = (b & 16) != 0 ? friendlyByteBuf.readResourceLocation() : null;
				return new ClientboundCommandsPacket.ArgumentNodeStub(string, template, resourceLocation);
			}
		} else if (i == 1) {
			String string = friendlyByteBuf.readUtf();
			return new ClientboundCommandsPacket.LiteralNodeStub(string);
		} else {
			return null;
		}
	}

	private static ClientboundCommandsPacket.Entry createEntry(
		CommandNode<SharedSuggestionProvider> commandNode, Object2IntMap<CommandNode<SharedSuggestionProvider>> object2IntMap
	) {
		int i = 0;
		int j;
		if (commandNode.getRedirect() != null) {
			i |= 8;
			j = object2IntMap.getInt(commandNode.getRedirect());
		} else {
			j = 0;
		}

		if (commandNode.getCommand() != null) {
			i |= 4;
		}

		ClientboundCommandsPacket.NodeStub nodeStub;
		if (commandNode instanceof RootCommandNode) {
			i |= 0;
			nodeStub = null;
		} else if (commandNode instanceof ArgumentCommandNode<SharedSuggestionProvider, ?> argumentCommandNode) {
			nodeStub = new ClientboundCommandsPacket.ArgumentNodeStub(argumentCommandNode);
			i |= 2;
			if (argumentCommandNode.getCustomSuggestions() != null) {
				i |= 16;
			}
		} else {
			if (!(commandNode instanceof LiteralCommandNode literalCommandNode)) {
				throw new UnsupportedOperationException("Unknown node type " + commandNode);
			}

			nodeStub = new ClientboundCommandsPacket.LiteralNodeStub(literalCommandNode.getLiteral());
			i |= 1;
		}

		int[] is = commandNode.getChildren().stream().mapToInt(object2IntMap::getInt).toArray();
		return new ClientboundCommandsPacket.Entry(nodeStub, i, j, is);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleCommands(this);
	}

	public RootCommandNode<SharedSuggestionProvider> getRoot(CommandBuildContext commandBuildContext) {
		return (RootCommandNode<SharedSuggestionProvider>)new ClientboundCommandsPacket.NodeResolver(commandBuildContext, this.entries).resolve(this.rootIndex);
	}

	static class ArgumentNodeStub implements ClientboundCommandsPacket.NodeStub {
		private final String id;
		private final ArgumentTypeInfo.Template<?> argumentType;
		@Nullable
		private final ResourceLocation suggestionId;

		@Nullable
		private static ResourceLocation getSuggestionId(@Nullable SuggestionProvider<SharedSuggestionProvider> suggestionProvider) {
			return suggestionProvider != null ? SuggestionProviders.getName(suggestionProvider) : null;
		}

		ArgumentNodeStub(String string, ArgumentTypeInfo.Template<?> template, @Nullable ResourceLocation resourceLocation) {
			this.id = string;
			this.argumentType = template;
			this.suggestionId = resourceLocation;
		}

		public ArgumentNodeStub(ArgumentCommandNode<SharedSuggestionProvider, ?> argumentCommandNode) {
			this(argumentCommandNode.getName(), ArgumentTypeInfos.unpack(argumentCommandNode.getType()), getSuggestionId(argumentCommandNode.getCustomSuggestions()));
		}

		@Override
		public ArgumentBuilder<SharedSuggestionProvider, ?> build(CommandBuildContext commandBuildContext) {
			ArgumentType<?> argumentType = this.argumentType.instantiate(commandBuildContext);
			RequiredArgumentBuilder<SharedSuggestionProvider, ?> requiredArgumentBuilder = RequiredArgumentBuilder.argument(this.id, argumentType);
			if (this.suggestionId != null) {
				requiredArgumentBuilder.suggests(SuggestionProviders.getProvider(this.suggestionId));
			}

			return requiredArgumentBuilder;
		}

		@Override
		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeUtf(this.id);
			serializeCap(friendlyByteBuf, this.argumentType);
			if (this.suggestionId != null) {
				friendlyByteBuf.writeResourceLocation(this.suggestionId);
			}
		}

		private static <A extends ArgumentType<?>> void serializeCap(FriendlyByteBuf friendlyByteBuf, ArgumentTypeInfo.Template<A> template) {
			serializeCap(friendlyByteBuf, template.type(), template);
		}

		private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void serializeCap(
			FriendlyByteBuf friendlyByteBuf, ArgumentTypeInfo<A, T> argumentTypeInfo, ArgumentTypeInfo.Template<A> template
		) {
			friendlyByteBuf.writeVarInt(Registry.COMMAND_ARGUMENT_TYPE.getId(argumentTypeInfo));
			argumentTypeInfo.serializeToNetwork((T)template, friendlyByteBuf);
		}
	}

	static class Entry {
		@Nullable
		final ClientboundCommandsPacket.NodeStub stub;
		final int flags;
		final int redirect;
		final int[] children;

		Entry(@Nullable ClientboundCommandsPacket.NodeStub nodeStub, int i, int j, int[] is) {
			this.stub = nodeStub;
			this.flags = i;
			this.redirect = j;
			this.children = is;
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeByte(this.flags);
			friendlyByteBuf.writeVarIntArray(this.children);
			if ((this.flags & 8) != 0) {
				friendlyByteBuf.writeVarInt(this.redirect);
			}

			if (this.stub != null) {
				this.stub.write(friendlyByteBuf);
			}
		}

		public boolean canBuild(IntSet intSet) {
			return (this.flags & 8) != 0 ? !intSet.contains(this.redirect) : true;
		}

		public boolean canResolve(IntSet intSet) {
			for (int i : this.children) {
				if (intSet.contains(i)) {
					return false;
				}
			}

			return true;
		}
	}

	static class LiteralNodeStub implements ClientboundCommandsPacket.NodeStub {
		private final String id;

		LiteralNodeStub(String string) {
			this.id = string;
		}

		@Override
		public ArgumentBuilder<SharedSuggestionProvider, ?> build(CommandBuildContext commandBuildContext) {
			return LiteralArgumentBuilder.literal(this.id);
		}

		@Override
		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeUtf(this.id);
		}
	}

	static class NodeResolver {
		private final CommandBuildContext context;
		private final List<ClientboundCommandsPacket.Entry> entries;
		private final List<CommandNode<SharedSuggestionProvider>> nodes;

		NodeResolver(CommandBuildContext commandBuildContext, List<ClientboundCommandsPacket.Entry> list) {
			this.context = commandBuildContext;
			this.entries = list;
			ObjectArrayList<CommandNode<SharedSuggestionProvider>> objectArrayList = new ObjectArrayList<>();
			objectArrayList.size(list.size());
			this.nodes = objectArrayList;
		}

		public CommandNode<SharedSuggestionProvider> resolve(int i) {
			CommandNode<SharedSuggestionProvider> commandNode = (CommandNode<SharedSuggestionProvider>)this.nodes.get(i);
			if (commandNode != null) {
				return commandNode;
			} else {
				ClientboundCommandsPacket.Entry entry = (ClientboundCommandsPacket.Entry)this.entries.get(i);
				CommandNode<SharedSuggestionProvider> commandNode2;
				if (entry.stub == null) {
					commandNode2 = new RootCommandNode<>();
				} else {
					ArgumentBuilder<SharedSuggestionProvider, ?> argumentBuilder = entry.stub.build(this.context);
					if ((entry.flags & 8) != 0) {
						argumentBuilder.redirect(this.resolve(entry.redirect));
					}

					if ((entry.flags & 4) != 0) {
						argumentBuilder.executes(commandContext -> 0);
					}

					commandNode2 = argumentBuilder.build();
				}

				this.nodes.set(i, commandNode2);

				for (int j : entry.children) {
					CommandNode<SharedSuggestionProvider> commandNode3 = this.resolve(j);
					if (!(commandNode3 instanceof RootCommandNode)) {
						commandNode2.addChild(commandNode3);
					}
				}

				return commandNode2;
			}
		}
	}

	interface NodeStub {
		ArgumentBuilder<SharedSuggestionProvider, ?> build(CommandBuildContext commandBuildContext);

		void write(FriendlyByteBuf friendlyByteBuf);
	}
}
