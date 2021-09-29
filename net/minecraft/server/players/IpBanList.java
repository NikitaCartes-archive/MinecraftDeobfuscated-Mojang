/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.io.File;
import java.net.SocketAddress;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.server.players.StoredUserList;
import org.jetbrains.annotations.Nullable;

public class IpBanList
extends StoredUserList<String, IpBanListEntry> {
    public IpBanList(File file) {
        super(file);
    }

    @Override
    protected StoredUserEntry<String> createEntry(JsonObject jsonObject) {
        return new IpBanListEntry(jsonObject);
    }

    public boolean isBanned(SocketAddress socketAddress) {
        String string = this.getIpFromAddress(socketAddress);
        return this.contains(string);
    }

    public boolean isBanned(String string) {
        return this.contains(string);
    }

    @Override
    @Nullable
    public IpBanListEntry get(SocketAddress socketAddress) {
        String string = this.getIpFromAddress(socketAddress);
        return (IpBanListEntry)this.get(string);
    }

    private String getIpFromAddress(SocketAddress socketAddress) {
        String string = socketAddress.toString();
        if (string.contains("/")) {
            string = string.substring(string.indexOf(47) + 1);
        }
        if (string.contains(":")) {
            string = string.substring(0, string.indexOf(58));
        }
        return string;
    }
}

