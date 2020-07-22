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
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.jetbrains.annotations.Nullable;

public class ClientboundCommandsPacket
implements Packet<ClientGamePacketListener> {
    private RootCommandNode<SharedSuggestionProvider> root;

    public ClientboundCommandsPacket() {
    }

    public ClientboundCommandsPacket(RootCommandNode<SharedSuggestionProvider> rootCommandNode) {
        this.root = rootCommandNode;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        Entry[] entrys = new Entry[friendlyByteBuf.readVarInt()];
        for (int i = 0; i < entrys.length; ++i) {
            entrys[i] = ClientboundCommandsPacket.readNode(friendlyByteBuf);
        }
        ClientboundCommandsPacket.resolveEntries(entrys);
        this.root = (RootCommandNode)entrys[friendlyByteBuf.readVarInt()].node;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        Object2IntMap<CommandNode<SharedSuggestionProvider>> object2IntMap = ClientboundCommandsPacket.enumerateNodes(this.root);
        CommandNode<SharedSuggestionProvider>[] commandNodes = ClientboundCommandsPacket.getNodesInIdOrder(object2IntMap);
        friendlyByteBuf.writeVarInt(commandNodes.length);
        for (CommandNode<SharedSuggestionProvider> commandNode : commandNodes) {
            ClientboundCommandsPacket.writeNode(friendlyByteBuf, commandNode, object2IntMap);
        }
        friendlyByteBuf.writeVarInt(object2IntMap.get(this.root));
    }

    private static void resolveEntries(Entry[] entrys) {
        ArrayList<Entry> list = Lists.newArrayList(entrys);
        while (!list.isEmpty()) {
            boolean bl = list.removeIf(entry -> entry.build(entrys));
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

    private static CommandNode<SharedSuggestionProvider>[] getNodesInIdOrder(Object2IntMap<CommandNode<SharedSuggestionProvider>> object2IntMap) {
        CommandNode[] commandNodes = new CommandNode[object2IntMap.size()];
        for (Object2IntMap.Entry entry : Object2IntMaps.fastIterable(object2IntMap)) {
            commandNodes[entry.getIntValue()] = (CommandNode)entry.getKey();
        }
        return commandNodes;
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
            String string = friendlyByteBuf.readUtf(Short.MAX_VALUE);
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
            return LiteralArgumentBuilder.literal(friendlyByteBuf.readUtf(Short.MAX_VALUE));
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

    @Environment(value=EnvType.CLIENT)
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

        public boolean build(Entry[] entrys) {
            if (this.node == null) {
                if (this.builder == null) {
                    this.node = new RootCommandNode<SharedSuggestionProvider>();
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
                if (entrys[i].node != null) continue;
                return false;
            }
            for (int i : this.children) {
                CommandNode<SharedSuggestionProvider> commandNode = entrys[i].node;
                if (commandNode instanceof RootCommandNode) continue;
                this.node.addChild(commandNode);
            }
            return true;
        }
    }
}

