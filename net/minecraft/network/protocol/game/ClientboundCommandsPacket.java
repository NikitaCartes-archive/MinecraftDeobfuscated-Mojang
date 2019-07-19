/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Maps;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
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
        ArrayDeque<Entry> deque = new ArrayDeque<Entry>(entrys.length);
        for (int i = 0; i < entrys.length; ++i) {
            entrys[i] = this.readNode(friendlyByteBuf);
            deque.add(entrys[i]);
        }
        while (!deque.isEmpty()) {
            boolean bl = false;
            Iterator iterator = deque.iterator();
            while (iterator.hasNext()) {
                Entry entry = (Entry)iterator.next();
                if (!entry.build(entrys)) continue;
                iterator.remove();
                bl = true;
            }
            if (bl) continue;
            throw new IllegalStateException("Server sent an impossible command tree");
        }
        this.root = (RootCommandNode)entrys[friendlyByteBuf.readVarInt()].node;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        HashMap<CommandNode<SharedSuggestionProvider>, Integer> map = Maps.newHashMap();
        ArrayDeque deque = new ArrayDeque();
        deque.add(this.root);
        while (!deque.isEmpty()) {
            CommandNode commandNode = (CommandNode)deque.pollFirst();
            if (map.containsKey(commandNode)) continue;
            int i = map.size();
            map.put(commandNode, i);
            deque.addAll(commandNode.getChildren());
            if (commandNode.getRedirect() == null) continue;
            deque.add(commandNode.getRedirect());
        }
        CommandNode[] commandNodes = new CommandNode[map.size()];
        for (Map.Entry entry : map.entrySet()) {
            commandNodes[((Integer)entry.getValue()).intValue()] = (CommandNode)entry.getKey();
        }
        friendlyByteBuf.writeVarInt(commandNodes.length);
        for (CommandNode commandNode2 : commandNodes) {
            this.writeNode(friendlyByteBuf, commandNode2, map);
        }
        friendlyByteBuf.writeVarInt((Integer)map.get(this.root));
    }

    private Entry readNode(FriendlyByteBuf friendlyByteBuf) {
        byte b = friendlyByteBuf.readByte();
        int[] is = friendlyByteBuf.readVarIntArray();
        int i = (b & 8) != 0 ? friendlyByteBuf.readVarInt() : 0;
        ArgumentBuilder<SharedSuggestionProvider, ?> argumentBuilder = this.createBuilder(friendlyByteBuf, b);
        return new Entry(argumentBuilder, b, i, is);
    }

    @Nullable
    private ArgumentBuilder<SharedSuggestionProvider, ?> createBuilder(FriendlyByteBuf friendlyByteBuf, byte b) {
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

    private void writeNode(FriendlyByteBuf friendlyByteBuf, CommandNode<SharedSuggestionProvider> commandNode, Map<CommandNode<SharedSuggestionProvider>, Integer> map) {
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

