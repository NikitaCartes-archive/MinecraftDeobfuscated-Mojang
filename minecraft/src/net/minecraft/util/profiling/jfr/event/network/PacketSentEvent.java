package net.minecraft.util.profiling.jfr.event.network;

import javax.annotation.Nullable;
import jdk.jfr.Category;
import jdk.jfr.DataAmount;
import jdk.jfr.Label;
import jdk.jfr.Name;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.util.profiling.jfr.event.AbstractMinecraftJfrEvent;

@Name("minecraft.PacketSent")
@Label("Packet sent")
@Category({"Minecraft", "Network"})
@DontObfuscate
public class PacketSentEvent extends AbstractMinecraftJfrEvent {
	public static final String NAME = "minecraft.PacketSent";
	public static final ThreadLocal<PacketSentEvent> EVENT = ThreadLocal.withInitial(PacketSentEvent::new);
	@Nullable
	@Label("Packet name")
	public String packetName;
	@Label("Bytes")
	@DataAmount
	public int bytes;

	private PacketSentEvent() {
	}

	public void reset() {
		this.packetName = null;
		this.bytes = 0;
	}
}
