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
            Component component = (Component)friendlyByteBuf.readNullable(FriendlyByteBuf::readComponent);
            return new Suggestion(stringRange, string, component);
        });
        this.suggestions = new Suggestions(stringRange, list);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.id);
        friendlyByteBuf.writeVarInt(this.suggestions.getRange().getStart());
        friendlyByteBuf.writeVarInt(this.suggestions.getRange().getLength());
        friendlyByteBuf.writeCollection(this.suggestions.getList(), (friendlyByteBuf2, suggestion) -> {
            friendlyByteBuf2.writeUtf(suggestion.getText());
            friendlyByteBuf2.writeNullable(suggestion.getTooltip(), (friendlyByteBuf, message) -> friendlyByteBuf.writeComponent(ComponentUtils.fromMessage(message)));
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

