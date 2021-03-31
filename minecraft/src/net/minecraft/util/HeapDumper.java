package net.minecraft.util;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import javax.annotation.Nullable;
import javax.management.MBeanServer;

public class HeapDumper {
	private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";
	@Nullable
	private static HotSpotDiagnosticMXBean hotspotMBean;

	private static HotSpotDiagnosticMXBean getHotspotMBean() {
		if (hotspotMBean == null) {
			try {
				MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
				hotspotMBean = (HotSpotDiagnosticMXBean)ManagementFactory.newPlatformMXBeanProxy(
					mBeanServer, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class
				);
			} catch (IOException var1) {
				throw new RuntimeException(var1);
			}
		}

		return hotspotMBean;
	}

	public static void dumpHeap(String string, boolean bl) {
		try {
			getHotspotMBean().dumpHeap(string, bl);
		} catch (IOException var3) {
			throw new RuntimeException(var3);
		}
	}
}
