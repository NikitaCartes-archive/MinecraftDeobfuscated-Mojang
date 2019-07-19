/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.client;

import com.mojang.realmsclient.dto.RegionPingResult;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class Ping {
    public static List<RegionPingResult> ping(Region ... regions) {
        for (Region region : regions) {
            Ping.ping(region.endpoint);
        }
        ArrayList<RegionPingResult> list = new ArrayList<RegionPingResult>();
        for (Region region2 : regions) {
            list.add(new RegionPingResult(region2.name, Ping.ping(region2.endpoint)));
        }
        Collections.sort(list, new Comparator<RegionPingResult>(){

            @Override
            public int compare(RegionPingResult regionPingResult, RegionPingResult regionPingResult2) {
                return regionPingResult.ping() - regionPingResult2.ping();
            }

            @Override
            public /* synthetic */ int compare(Object object, Object object2) {
                return this.compare((RegionPingResult)object, (RegionPingResult)object2);
            }
        });
        return list;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static int ping(String string) {
        int i = 700;
        long l = 0L;
        Socket socket = null;
        for (int j = 0; j < 5; ++j) {
            try {
                InetSocketAddress socketAddress = new InetSocketAddress(string, 80);
                socket = new Socket();
                long m = Ping.now();
                socket.connect(socketAddress, 700);
                l += Ping.now() - m;
                Ping.close(socket);
                continue;
            } catch (Exception exception) {
                l += 700L;
                continue;
            } finally {
                Ping.close(socket);
            }
        }
        return (int)((double)l / 5.0);
    }

    private static void close(Socket socket) {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    public static List<RegionPingResult> pingAllRegions() {
        return Ping.ping(Region.values());
    }

    @Environment(value=EnvType.CLIENT)
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

