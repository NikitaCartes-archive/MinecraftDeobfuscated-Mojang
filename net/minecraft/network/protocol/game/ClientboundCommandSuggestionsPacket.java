/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundCommandSuggestionsPacket
implements Packet<ClientGamePacketListener> {
    private final int id;
    private final Suggestions suggestions;

    public ClientboundCommandSuggestionsPacket(int i, Suggestions suggestions) {
        this.id = i;
        this.suggestions = suggestions;
    }

    public ClientboundCommandSuggestionsPacket(FriendlyByteBuf friendlyByteBuf2) {
        this.id = friendlyByteBuf2.readVarInt();
        int i = friendlyByteBuf2.readVarInt();
        int j = friendlyByteBuf2.readVarInt();
        StringRange stringRange = StringRange.between(i, i + j);
        List<Suggestion> list = friendlyByteBuf2.readList(friendlyByteBuf -> {
            String string = friendlyByteBuf.readUtf();
            Component component = friendlyByteBuf.readBoolean() ? friendlyByteBuf.readComponent() : null;
            return new Suggestion(stringRange, string, component);
        });
        this.suggestions = new Suggestions(stringRange, list);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeVarInt(this.id);
        friendlyByteBuf2.writeVarInt(this.suggestions.getRange().getStart());
        friendlyByteBuf2.writeVarInt(this.suggestions.getRange().getLength());
        friendlyByteBuf2.writeCollection(this.suggestions.getList(), (friendlyByteBuf, suggestion) -> {
            friendlyByteBuf.writeUtf(suggestion.getText());
            friendlyByteBuf.writeBoolean(suggestion.getTooltip() != null);
            if (suggestion.getTooltip() != null) {
                friendlyByteBuf.writeComponent(ComponentUtils.fromMessage(suggestion.getTooltip()));
            }
        });
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleCommandSuggestions(this);
    }

    public int getId() {
        return this.id;
    }

    public Suggestions getSuggestions() {
        return this.suggestions;
    }
}

