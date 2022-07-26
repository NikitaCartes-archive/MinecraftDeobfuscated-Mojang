/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network;

import java.util.function.Supplier;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.Nullable;

public interface PacketSendListener {
    public static PacketSendListener thenRun(final Runnable runnable) {
        return new PacketSendListener(){

            @Override
            public void onSuccess() {
                runnable.run();
            }

            @Override
            @Nullable
            public Packet<?> onFailure() {
                runnable.run();
                return null;
            }
        };
    }

    public static PacketSendListener exceptionallySend(final Supplier<Packet<?>> supplier) {
        return new PacketSendListener(){

            @Override
            @Nullable
            public Packet<?> onFailure() {
                return (Packet)supplier.get();
            }
        };
    }

    default public void onSuccess() {
    }

    @Nullable
    default public Packet<?> onFailure() {
        return null;
    }
}

