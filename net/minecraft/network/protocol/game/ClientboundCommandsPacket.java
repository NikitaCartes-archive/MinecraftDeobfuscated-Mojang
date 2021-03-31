/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
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
    private final RootCommandNode<SharedSuggestionProvider> root;

    public ClientboundCommandsPacket(RootCommandNode<SharedSuggestionProvider> rootCommandNode) {
        this.root = rootCommandNode;
    }

    public ClientboundCommandsPacket(FriendlyByteBuf friendlyByteBuf) {
        List<Entry> list = friendlyByteBuf.readList(ClientboundCommandsPacket::readNode);
        ClientboundCommandsPacket.resolveEntries(list);
        int i = friendlyByteBuf.readVarInt();
        this.root = (RootCommandNode)list.get(i).node;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf2) {
        Object2IntMap<CommandNode<SharedSuggestionProvider>> object2IntMap = ClientboundCommandsPacket.enumerateNodes(this.root);
        List<CommandNode<SharedSuggestionProvider>> list = ClientboundCommandsPacket.getNodesInIdOrder(object2IntMap);
        friendlyByteBuf2.writeCollection(list, (friendlyByteBuf, commandNode) -> ClientboundCommandsPacket.writeNode(friendlyByteBuf, commandNode, object2IntMap));
        friendlyByteBuf2.writeVarInt(object2IntMap.get(this.root));
    }

    private static void resolveEntries(List<Entry> list) {
        ArrayList<Entry> list2 = Lists.newArrayList(list);
        while (!list2.isEmpty()) {
            boolean bl = list2.removeIf(entry -> entry.build(list));
            if (bl) continue;
            throw new IllegalStateException("Server sent an impossible command tree");
        }
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

    private static List<CommandNode<SharedSuggestionProvider>> getNodesInIdOrder(Object2IntMap<CommandNode<SharedSuggestionProvider>> object2IntMap) {
        ObjectArrayList<CommandNode<SharedSuggestionProvider>> objectArrayList = new ObjectArrayList<CommandNode<SharedSuggestionProvider>>(object2IntMap.size());
        objectArrayList.size(object2IntMap.size());
        for (Object2IntMap.Entry entry : Object2IntMaps.fastIterable(object2IntMap)) {
            objectArrayList.set(entry.getIntValue(), (CommandNode<SharedSuggestionProvider>)entry.getKey());
        }
        return objectArrayList;
    }

    private static Entry readNode(FriendlyByteBuf friendlyByteBuf) {
        byte b = friendlyByteBuf.readByte();
        int[] is = friendlyByteBuf.readVarIntArray();
        int i = (b & 8) != 0 ? friendlyByteBuf.readVarInt() : 0;
        ArgumentBuilder<SharedSuggestionProvider, ?> argumentBuilder = ClientboundCommandsPacket.createBuilder(friendlyByteBuf, b);
        return new Entry(argumentBuilder, b, i, is);
    }

    @Nullable
    private static ArgumentBuilder<SharedSuggestionProvider, ?> createBuilder(FriendlyByteBuf friendlyByteBuf, byte b) {
        int i = b & 3;
        if (i == 2) {
            String string = friendlyByteBuf.readUtf();
            ArgumentType<?> argumentType = ArgumentTypes.deserialize(friendlyByteBuf);
            if (argumentType == null) {
                return null;
            }
            RequiredArgumentBuilder<SharedSuggestionProvider, ?> requiredArgumentBuilder = RequiredArgumentBuilder.argument(string, argumentType);
            if ((b & 0x10) != 0) {
                requiredArgumentBuilder.suggests(SuggestionProviders.getProvider(friendlyByteBuf.readResourceLocation()));
            }
            return requiredArgumentBuilder;
        }
        if (i == 1) {
            return LiteralArgumentBuilder.literal(friendlyByteBuf.readUtf());
        }
        return null;
    }

    private static void writeNode(FriendlyByteBuf friendlyByteBuf, CommandNode<SharedSuggestionProvider> commandNode, Map<CommandNode<SharedSuggestionProvider>, Integer> map) {
        int b = 0;
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
                b = (byte)(b | 0x10);
            }
        } else if (commandNode instanceof LiteralCommandNode) {
            b = (byte)(b | 1);
        } else {
            throw new UnsupportedOperationException("Unknown node type " + commandNode);
        }
        friendlyByteBuf.writeByte(b);
        friendlyByteBuf.writeVarInt(commandNode.getChildren().size());
        for (CommandNode<SharedSuggestionProvider> commandNode2 : commandNode.getChildren()) {
            friendlyByteBuf.writeVarInt(map.get(commandNode2));
        }
        if (commandNode.getRedirect() != null) {
            friendlyByteBuf.writeVarInt(map.get(commandNode.getRedirect()));
        }
        if (commandNode instanceof ArgumentCommandNode) {
            ArgumentCommandNode argumentCommandNode = (ArgumentCommandNode)commandNode;
            friendlyByteBuf.writeUtf(argumentCommandNode.getName());
            ArgumentTypes.serialize(friendlyByteBuf, argumentCommandNode.getType());
            if (argumentCommandNode.getCustomSuggestions() != null) {
                friendlyByteBuf.writeResourceLocation(SuggestionProviders.getName(argumentCommandNode.getCustomSuggestions()));
            }
        } else if (commandNode instanceof LiteralCommandNode) {
            friendlyByteBuf.writeUtf(((LiteralCommandNode)commandNode).getLiteral());
        }
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleCommands(this);
    }

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

        public boolean build(List<Entry> list) {
            if (this.node == null) {
                if (this.builder == null) {
                    this.node = new RootCommandNode<SharedSuggestionProvider>();
                } else {
                    if ((this.flags & 8) != 0) {
                        if (list.get((int)this.redirect).node == null) {
                            return false;
                        }
                        this.builder.redirect(list.get((int)this.redirect).node);
                    }
                    if ((this.flags & 4) != 0) {
                        this.builder.executes(commandContext -> 0);
                    }
                    this.node = this.builder.build();
                }
            }
            for (int i : this.children) {
                if (list.get((int)i).node != null) continue;
                return false;
            }
            for (int i : this.children) {
                CommandNode<SharedSuggestionProvider> commandNode = list.get((int)i).node;
                if (commandNode instanceof RootCommandNode) continue;
                this.node.addChild(commandNode);
            }
            return true;
        }
    }
}

