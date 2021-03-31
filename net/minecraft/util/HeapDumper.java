/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import org.jetbrains.annotations.Nullable;

public class HeapDumper {
    private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";
    @Nullable
    private static HotSpotDiagnosticMXBean hotspotMBean;

    private static HotSpotDiagnosticMXBean getHotspotMBean() {
        if (hotspotMBean == null) {
            try {
                MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
                hotspotMBean = ManagementFactory.newPlatformMXBeanProxy(mBeanServer, HOTSPOT_BEAN_NAME, HotSpotDiagnosticMXBean.class);
            } catch (IOException iOException) {
                throw new RuntimeException(iOException);
            }
        }
        return hotspotMBean;
    }

    public static void dumpHeap(String string, boolean bl) {
        try {
            HeapDumper.getHotspotMBean().dumpHeap(string, bl);
        } catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
    }
}

