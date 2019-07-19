/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.io.IOException;
import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundCommandSuggestionsPacket
implements Packet<ClientGamePacketListener> {
    private int id;
    private Suggestions suggestions;

    public ClientboundCommandSuggestionsPacket() {
    }

    public ClientboundCommandSuggestionsPacket(int i, Suggestions suggestions) {
        this.id = i;
        this.suggestions = suggestions;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.id = friendlyByteBuf.readVarInt();
        int i = friendlyByteBuf.readVarInt();
        int j = friendlyByteBuf.readVarInt();
        StringRange stringRange = StringRange.between(i, i + j);
        int k = friendlyByteBuf.readVarInt();
        ArrayList<Suggestion> list = Lists.newArrayListWithCapacity(k);
        for (int l = 0; l < k; ++l) {
            String string = friendlyByteBuf.readUtf(Short.MAX_VALUE);
            Component component = friendlyByteBuf.readBoolean() ? friendlyByteBuf.readComponent() : null;
            list.add(new Suggestion(stringRange, string, component));
        }
        this.suggestions = new Suggestions(stringRange, list);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeVarInt(this.id);
        friendlyByteBuf.writeVarInt(this.suggestions.getRange().getStart());
        friendlyByteBuf.writeVarInt(this.suggestions.getRange().getLength());
        friendlyByteBuf.writeVarInt(this.suggestions.getList().size());
        for (Suggestion suggestion : this.suggestions.getList()) {
            friendlyByteBuf.writeUtf(suggestion.getText());
            friendlyByteBuf.writeBoolean(suggestion.getTooltip() != null);
            if (suggestion.getTooltip() == null) continue;
            friendlyByteBuf.writeComponent(ComponentUtils.fromMessage(suggestion.getTooltip()));
        }
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleCommandSuggestions(this);
    }

    @Environment(value=EnvType.CLIENT)
    public int getId() {
        return this.id;
    }

    @Environment(value=EnvType.CLIENT)
    public Suggestions getSuggestions() {
        return this.suggestions;
    }
}

