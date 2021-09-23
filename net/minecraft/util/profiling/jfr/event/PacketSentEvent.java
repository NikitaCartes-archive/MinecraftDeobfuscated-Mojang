/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling.jfr.event;

import java.net.SocketAddress;
import jdk.jfr.EventType;
import jdk.jfr.Label;
import jdk.jfr.Name;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.util.profiling.jfr.event.PacketEvent;

@Name(value="minecraft.PacketSent")
@Label(value="Network Packet Sent")
@DontObfuscate
public class PacketSentEvent
extends PacketEvent {
    public static final String NAME = "minecraft.PacketSent";
    public static final EventType TYPE = EventType.getEventType(PacketSentEvent.class);

    public PacketSentEvent(String string, SocketAddress socketAddress, int i) {
        super(string, socketAddress, i);
    }
}

