package com.mojang.realmsclient.client;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.dto.RegionPingResult;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;

@Environment(EnvType.CLIENT)
public class Ping {
	public static List<RegionPingResult> ping(Ping.Region... regions) {
		for (Ping.Region region : regions) {
			ping(region.endpoint);
		}

		List<RegionPingResult> list = Lists.<RegionPingResult>newArrayList();

		for (Ping.Region region2 : regions) {
			list.add(new RegionPingResult(region2.name, ping(region2.endpoint)));
		}

		list.sort(Comparator.comparingInt(RegionPingResult::ping));
		return list;
	}

	private static int ping(String string) {
		int i = 700;
		long l = 0L;
		Socket socket = null;

		for (int j = 0; j < 5; j++) {
			try {
				SocketAddress socketAddress = new InetSocketAddress(string, 80);
				socket = new Socket();
				long m = now();
				socket.connect(socketAddress, 700);
				l += now() - m;
			} catch (Exception var12) {
				l += 700L;
			} finally {
				close(socket);
			}
		}

		return (int)((double)l / 5.0);
	}

	private static void close(Socket socket) {
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (Throwable var2) {
		}
	}

	private static long now() {
		return Util.getMillis();
	}

	public static List<RegionPingResult> pingAllRegions() {
		return ping(Ping.Region.values());
	}

	@Environment(EnvType.CLIENT)
	static enum Region {
		US_EAST_1("us-east-1", "ec2.us-east-1.amazonaws.com"),
		US_WEST_2("us-west-2", "ec2.us-west-2.amazonaws.com"),
		US_WEST_1("us-west-1", "ec2.us-west-1.amazonaws.com"),
		EU_WEST_1("eu-west-1", "ec2.eu-west-1.amazonaws.com"),
		AP_SOUTHEAST_1("ap-southeast-1", "ec2.ap-southeast-1.amazonaws.com"),
		AP_SOUTHEAST_2("ap-southeast-2", "ec2.ap-southeast-2.amazonaws.com"),
		AP_NORTHEAST_1("ap-northeast-1", "ec2.ap-northeast-1.amazonaws.com"),
		SA_EAST_1("sa-east-1", "ec2.sa-east-1.amazonaws.com");

		private final String name;
		private final String endpoint;

		private Region(String string2, String string3) {
			this.name = string2;
			this.endpoint = string3;
		}
	}
}
