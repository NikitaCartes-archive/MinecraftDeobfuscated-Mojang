/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands;

import java.util.UUID;
import net.minecraft.network.chat.Component;

public interface CommandSource {
    public static final CommandSource NULL = new CommandSource(){

        @Override
        public void sendMessage(Component component, UUID uUID) {
        }

        @Override
        public boolean acceptsSuccess() {
            return false;
        }

        @Override
        public boolean acceptsFailure() {
            return false;
        }

        @Override
        public boolean shouldInformAdmins() {
            return false;
        }
    };

    public void sendMessage(Component var1, UUID var2);

    public boolean acceptsSuccess();

    public boolean acceptsFailure();

    public boolean shouldInformAdmins();

    default public boolean alwaysAccepts() {
        return false;
    }
}

