package net.minecraft.util.profiling.jfr.event.network;

import javax.annotation.Nullable;
import jdk.jfr.Category;
import jdk.jfr.DataAmount;
import jdk.jfr.Label;
import jdk.jfr.Name;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.util.profiling.jfr.event.AbstractMinecraftJfrEvent;

@Name("minecraft.PacketRead")
@Label("Packet read")
@Category({"Minecraft", "Network"})
@DontObfuscate
public class PacketReceivedEvent extends AbstractMinecraftJfrEvent {
	public static final String NAME = "minecraft.PacketRead";
	public static final ThreadLocal<PacketReceivedEvent> EVENT = ThreadLocal.withInitial(PacketReceivedEvent::new);
	@Nullable
	@Label("Packet name")
	public String packetName;
	@Label("Bytes")
	@DataAmount
	public int bytes;

	private PacketReceivedEvent() {
	}

	public void reset() {
		this.packetName = null;
		this.bytes = 0;
	}
}
