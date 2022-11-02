package net.minecraft.client.multiplayer.resolver;

import com.google.common.net.HostAndPort;
import com.mojang.logging.LogUtils;
import java.net.IDN;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public final class ServerAddress {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final HostAndPort hostAndPort;
	private static final ServerAddress INVALID = new ServerAddress(HostAndPort.fromParts("server.invalid", 25565));

	public ServerAddress(String string, int i) {
		this(HostAndPort.fromParts(string, i));
	}

	private ServerAddress(HostAndPort hostAndPort) {
		this.hostAndPort = hostAndPort;
	}

	public String getHost() {
		try {
			return IDN.toASCII(this.hostAndPort.getHost());
		} catch (IllegalArgumentException var2) {
			return "";
		}
	}

	public int getPort() {
		return this.hostAndPort.getPort();
	}

	public static ServerAddress parseString(String string) {
		if (string == null) {
			return INVALID;
		} else {
			try {
				HostAndPort hostAndPort = HostAndPort.fromString(string).withDefaultPort(25565);
				return hostAndPort.getHost().isEmpty() ? INVALID : new ServerAddress(hostAndPort);
			} catch (IllegalArgumentException var2) {
				LOGGER.info("Failed to parse URL {}", string, var2);
				return INVALID;
			}
		}
	}

	public static boolean isValidAddress(String string) {
		try {
			HostAndPort hostAndPort = HostAndPort.fromString(string);
			String string2 = hostAndPort.getHost();
			if (!string2.isEmpty()) {
				IDN.toASCII(string2);
				return true;
			}
		} catch (IllegalArgumentException var3) {
		}

		return false;
	}

	static int parsePort(String string) {
		try {
			return Integer.parseInt(string.trim());
		} catch (Exception var2) {
			return 25565;
		}
	}

	public String toString() {
		return this.hostAndPort.toString();
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return object instanceof ServerAddress ? this.hostAndPort.equals(((ServerAddress)object).hostAndPort) : false;
		}
	}

	public int hashCode() {
		return this.hostAndPort.hashCode();
	}
}
