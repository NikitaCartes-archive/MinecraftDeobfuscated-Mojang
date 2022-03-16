/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import java.util.ArrayDeque;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class ClientboundCommandsPacket
implements Packet<ClientGamePacketListener> {
    private static final byte MASK_TYPE = 3;
    private static final byte FLAG_EXECUTABLE = 4;
    private static final byte FLAG_REDIRECT = 8;
    private static final byte FLAG_CUSTOM_SUGGESTIONS = 16;
    private static final byte TYPE_ROOT = 0;
    private static final byte TYPE_LITERAL = 1;
    private static final byte TYPE_ARGUMENT = 2;
    private final int rootIndex;
    private final List<Entry> entries;

    public ClientboundCommandsPacket(RootCommandNode<SharedSuggestionProvider> rootCommandNode) {
        Object2IntMap<CommandNode<SharedSuggestionProvider>> object2IntMap = ClientboundCommandsPacket.enumerateNodes(rootCommandNode);
        this.entries = ClientboundCommandsPacket.createEntries(object2IntMap);
        this.rootIndex = object2IntMap.getInt(rootCommandNode);
    }

    public ClientboundCommandsPacket(FriendlyByteBuf friendlyByteBuf) {
        this.entries = friendlyByteBuf.readList(ClientboundCommandsPacket::readNode);
        this.rootIndex = friendlyByteBuf.readVarInt();
        ClientboundCommandsPacket.validateEntries(this.entries);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeCollection(this.entries, (friendlyByteBuf, entry) -> entry.write((FriendlyByteBuf)friendlyByteBuf));
        friendlyByteBuf2.writeVarInt(this.rootIndex);
    }

    private static void validateEntries(List<Entry> list, BiPredicate<Entry, IntSet> biPredicate) {
        IntOpenHashSet intSet = new IntOpenHashSet(IntSets.fromTo(0, list.size()));
        while (!intSet.isEmpty()) {
            boolean bl = intSet.removeIf(i -> biPredicate.test((Entry)list.get(i), intSet));
            if (bl) continue;
            throw new IllegalStateException("Server sent an impossible command tree");
        }
    }

    private static void validateEntries(List<Entry> list) {
        ClientboundCommandsPacket.validateEntries(list, Entry::canBuild);
        ClientboundCommandsPacket.validateEntries(list, Entry::canResolve);
    }

    private static Object2IntMap<CommandNode<SharedSuggestionProvider>> enumerateNodes(RootCommandNode<SharedSuggestionProvider> rootCommandNode) {
        CommandNode commandNode;
        Object2IntOpenHashMap<CommandNode<SharedSuggestionProvider>> object2IntMap = new Object2IntOpenHashMap<CommandNode<SharedSuggestionProvider>>();
        ArrayDeque queue = Queues.newArrayDeque();
        queue.add(rootCommandNode);
        while ((commandNode = (CommandNode)queue.poll()) != null) {
            if (object2IntMap.containsKey(commandNode)) continue;
            int i = object2IntMap.size();
            object2IntMap.put((CommandNode<SharedSuggestionProvider>)commandNode, i);
            queue.addAll(commandNode.getChildren());
            if (commandNode.getRedirect() == null) continue;
            queue.add(commandNode.getRedirect());
        }
        return object2IntMap;
    }

    private static List<Entry> createEntries(Object2IntMap<CommandNode<SharedSuggestionProvider>> object2IntMap) {
        ObjectArrayList<Entry> objectArrayList = new ObjectArrayList<Entry>(object2IntMap.size());
        objectArrayList.size(object2IntMap.size());
        for (Object2IntMap.Entry entry : Object2IntMaps.fastIterable(object2IntMap)) {
            objectArrayList.set(entry.getIntValue(), ClientboundCommandsPacket.createEntry((CommandNode)entry.getKey(), object2IntMap));
        }
        return objectArrayList;
    }

    private static Entry readNode(FriendlyByteBuf friendlyByteBuf) {
        byte b = friendlyByteBuf.readByte();
        int[] is = friendlyByteBuf.readVarIntArray();
        int i = (b & 8) != 0 ? friendlyByteBuf.readVarInt() : 0;
        NodeStub nodeStub = ClientboundCommandsPacket.read(friendlyByteBuf, b);
        return new Entry(nodeStub, b, i, is);
    }

    @Nullable
    private static NodeStub read(FriendlyByteBuf friendlyByteBuf, byte b) {
        int i = b & 3;
        if (i == 2) {
            String string = friendlyByteBuf.readUtf();
            int j = friendlyByteBuf.readVarInt();
            ArgumentTypeInfo argumentTypeInfo = (ArgumentTypeInfo)Registry.COMMAND_ARGUMENT_TYPE.byId(j);
            if (argumentTypeInfo == null) {
                return null;
            }
            Object template = argumentTypeInfo.deserializeFromNetwork(friendlyByteBuf);
            ResourceLocation resourceLocation = (b & 0x10) != 0 ? friendlyByteBuf.readResourceLocation() : null;
            return new ArgumentNodeStub(string, (ArgumentTypeInfo.Template<?>)template, resourceLocation);
        }
        if (i == 1) {
            String string = friendlyByteBuf.readUtf();
            return new LiteralNodeStub(string);
        }
        return null;
    }

    private static Entry createEntry(CommandNode<SharedSuggestionProvider> commandNode, Object2IntMap<CommandNode<SharedSuggestionProvider>> object2IntMap) {
        NodeStub nodeStub;
        int j;
        int i = 0;
        if (commandNode.getRedirect() != null) {
            i |= 8;
            j = object2IntMap.getInt(commandNode.getRedirect());
        } else {
            j = 0;
        }
        if (commandNode.getCommand() != null) {
            i |= 4;
        }
        if (commandNode instanceof RootCommandNode) {
            i |= 0;
            nodeStub = null;
        } else if (commandNode instanceof ArgumentCommandNode) {
            ArgumentCommandNode argumentCommandNode = (ArgumentCommandNode)commandNode;
            nodeStub = new ArgumentNodeStub(argumentCommandNode);
            i |= 2;
            if (argumentCommandNode.getCustomSuggestions() != null) {
                i |= 0x10;
            }
        } else if (commandNode instanceof LiteralCommandNode) {
            LiteralCommandNode literalCommandNode = (LiteralCommandNode)commandNode;
            nodeStub = new LiteralNodeStub(literalCommandNode.getLiteral());
            i |= 1;
        } else {
            throw new UnsupportedOperationException("Unknown node type " + commandNode);
        }
        int[] is = commandNode.getChildren().stream().mapToInt(object2IntMap::getInt).toArray();
        return new Entry(nodeStub, i, j, is);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleCommands(this);
    }

    public RootCommandNode<SharedSuggestionProvider> getRoot(CommandBuildContext commandBuildContext) {
        return (RootCommandNode)new NodeResolver(commandBuildContext, this.entries).resolve(this.rootIndex);
    }

    static class Entry {
        @Nullable
        final NodeStub stub;
        final int flags;
        final int redirect;
        final int[] children;

        Entry(@Nullable NodeStub nodeStub, int i, int j, int[] is) {
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
            if ((this.flags & 8) != 0) {
                return !intSet.contains(this.redirect);
            }
            return true;
        }

        public boolean canResolve(IntSet intSet) {
            for (int i : this.children) {
                if (!intSet.contains(i)) continue;
                return false;
            }
            return true;
        }
    }

    static interface NodeStub {
        public ArgumentBuilder<SharedSuggestionProvider, ?> build(CommandBuildContext var1);

        public void write(FriendlyByteBuf var1);
    }

    static class ArgumentNodeStub
    implements NodeStub {
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
            this(argumentCommandNode.getName(), ArgumentTypeInfos.unpack(argumentCommandNode.getType()), ArgumentNodeStub.getSuggestionId(argumentCommandNode.getCustomSuggestions()));
        }

        @Override
        public ArgumentBuilder<SharedSuggestionProvider, ?> build(CommandBuildContext commandBuildContext) {
            Object argumentType = this.argumentType.instantiate(commandBuildContext);
            RequiredArgumentBuilder requiredArgumentBuilder = RequiredArgumentBuilder.argument(this.id, argumentType);
            if (this.suggestionId != null) {
                requiredArgumentBuilder.suggests(SuggestionProviders.getProvider(this.suggestionId));
            }
            return requiredArgumentBuilder;
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeUtf(this.id);
            ArgumentNodeStub.serializeCap(friendlyByteBuf, this.argumentType);
            if (this.suggestionId != null) {
                friendlyByteBuf.writeResourceLocation(this.suggestionId);
            }
        }

        private static <A extends ArgumentType<?>> void serializeCap(FriendlyByteBuf friendlyByteBuf, ArgumentTypeInfo.Template<A> template) {
            ArgumentNodeStub.serializeCap(friendlyByteBuf, template.type(), template);
        }

        private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void serializeCap(FriendlyByteBuf friendlyByteBuf, ArgumentTypeInfo<A, T> argumentTypeInfo, ArgumentTypeInfo.Template<A> template) {
            friendlyByteBuf.writeVarInt(Registry.COMMAND_ARGUMENT_TYPE.getId(argumentTypeInfo));
            argumentTypeInfo.serializeToNetwork(template, friendlyByteBuf);
        }
    }

    static class LiteralNodeStub
    implements NodeStub {
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
        private final List<Entry> entries;
        private final List<CommandNode<SharedSuggestionProvider>> nodes;

        NodeResolver(CommandBuildContext commandBuildContext, List<Entry> list) {
            this.context = commandBuildContext;
            this.entries = list;
            ObjectArrayList<CommandNode<SharedSuggestionProvider>> objectArrayList = new ObjectArrayList<CommandNode<SharedSuggestionProvider>>();
            objectArrayList.size(list.size());
            this.nodes = objectArrayList;
        }

        public CommandNode<SharedSuggestionProvider> resolve(int i) {
            RootCommandNode<SharedSuggestionProvider> commandNode2;
            CommandNode<SharedSuggestionProvider> commandNode = this.nodes.get(i);
            if (commandNode != null) {
                return commandNode;
            }
            Entry entry = this.entries.get(i);
            if (entry.stub == null) {
                commandNode2 = new RootCommandNode();
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
                if (commandNode3 instanceof RootCommandNode) continue;
                commandNode2.addChild(commandNode3);
            }
            return commandNode2;
        }
    }
}

