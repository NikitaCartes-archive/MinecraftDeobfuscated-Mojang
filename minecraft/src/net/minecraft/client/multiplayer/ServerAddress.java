package net.minecraft.client.multiplayer;

import com.mojang.datafixers.util.Pair;
import java.net.IDN;
import java.util.Hashtable;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ServerAddress {
	private final String host;
	private final int port;

	private ServerAddress(String string, int i) {
		this.host = string;
		this.port = i;
	}

	public String getHost() {
		try {
			return IDN.toASCII(this.host);
		} catch (IllegalArgumentException var2) {
			return "";
		}
	}

	public int getPort() {
		return this.port;
	}

	public static ServerAddress parseString(String string) {
		if (string == null) {
			return null;
		} else {
			String[] strings = string.split(":");
			if (string.startsWith("[")) {
				int i = string.indexOf("]");
				if (i > 0) {
					String string2 = string.substring(1, i);
					String string3 = string.substring(i + 1).trim();
					if (string3.startsWith(":") && !string3.isEmpty()) {
						string3 = string3.substring(1);
						strings = new String[]{string2, string3};
					} else {
						strings = new String[]{string2};
					}
				}
			}

			if (strings.length > 2) {
				strings = new String[]{string};
			}

			String string4 = strings[0];
			int j = strings.length > 1 ? parseInt(strings[1], 25565) : 25565;
			if (j == 25565) {
				Pair<String, Integer> pair = lookupSrv(string4);
				string4 = pair.getFirst();
				j = pair.getSecond();
			}

			return new ServerAddress(string4, j);
		}
	}

	private static Pair<String, Integer> lookupSrv(String string) {
		try {
			String string2 = "com.sun.jndi.dns.DnsContextFactory";
			Class.forName("com.sun.jndi.dns.DnsContextFactory");
			Hashtable<String, String> hashtable = new Hashtable();
			hashtable.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
			hashtable.put("java.naming.provider.url", "dns:");
			hashtable.put("com.sun.jndi.dns.timeout.retries", "1");
			DirContext dirContext = new InitialDirContext(hashtable);
			Attributes attributes = dirContext.getAttributes("_minecraft._tcp." + string, new String[]{"SRV"});
			Attribute attribute = attributes.get("srv");
			if (attribute != null) {
				String[] strings = attribute.get().toString().split(" ", 4);
				return Pair.of(strings[3], parseInt(strings[2], 25565));
			}
		} catch (Throwable var7) {
		}

		return Pair.of(string, 25565);
	}

	private static int parseInt(String string, int i) {
		try {
			return Integer.parseInt(string.trim());
		} catch (Exception var3) {
			return i;
		}
	}
}
