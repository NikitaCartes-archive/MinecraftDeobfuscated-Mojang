package net.minecraft.server.network;

import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;

public interface ConfigurationTask {
	void start(Consumer<Packet<?>> consumer);

	ConfigurationTask.Type type();

	public static record Type(String id) {
		public String toString() {
			return this.id;
		}
	}
}
