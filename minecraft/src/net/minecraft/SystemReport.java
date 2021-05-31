package net.minecraft;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PhysicalMemory;
import oshi.hardware.VirtualMemory;
import oshi.hardware.CentralProcessor.ProcessorIdentifier;

public class SystemReport {
	public static final long BYTES_PER_MEBIBYTE = 1048576L;
	private static final long ONE_GIGA = 1000000000L;
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String OPERATING_SYSTEM = System.getProperty("os.name")
		+ " ("
		+ System.getProperty("os.arch")
		+ ") version "
		+ System.getProperty("os.version");
	private static final String JAVA_VERSION = System.getProperty("java.version") + ", " + System.getProperty("java.vendor");
	private static final String JAVA_VM_VERSION = System.getProperty("java.vm.name")
		+ " ("
		+ System.getProperty("java.vm.info")
		+ "), "
		+ System.getProperty("java.vm.vendor");
	private final Map<String, String> entries = Maps.<String, String>newLinkedHashMap();

	public SystemReport() {
		this.setDetail("Minecraft Version", SharedConstants.getCurrentVersion().getName());
		this.setDetail("Minecraft Version ID", SharedConstants.getCurrentVersion().getId());
		this.setDetail("Operating System", OPERATING_SYSTEM);
		this.setDetail("Java Version", JAVA_VERSION);
		this.setDetail("Java VM Version", JAVA_VM_VERSION);
		this.setDetail("Memory", (Supplier<String>)(() -> {
			Runtime runtime = Runtime.getRuntime();
			long l = runtime.maxMemory();
			long m = runtime.totalMemory();
			long n = runtime.freeMemory();
			long o = l / 1048576L;
			long p = m / 1048576L;
			long q = n / 1048576L;
			return n + " bytes (" + q + " MiB) / " + m + " bytes (" + p + " MiB) up to " + l + " bytes (" + o + " MiB)";
		}));
		this.setDetail("CPUs", (Supplier<String>)(() -> String.valueOf(Runtime.getRuntime().availableProcessors())));
		this.ignoreErrors("hardware", () -> this.putHardware(new SystemInfo()));
		this.setDetail("JVM Flags", (Supplier<String>)(() -> {
			List<String> list = (List<String>)Util.getVmArguments().collect(Collectors.toList());
			return String.format("%d total; %s", list.size(), String.join(" ", list));
		}));
	}

	public void setDetail(String string, String string2) {
		this.entries.put(string, string2);
	}

	public void setDetail(String string, Supplier<String> supplier) {
		try {
			this.setDetail(string, (String)supplier.get());
		} catch (Exception var4) {
			LOGGER.warn("Failed to get system info for {}", string, var4);
			this.setDetail(string, "ERR");
		}
	}

	private void putHardware(SystemInfo systemInfo) {
		HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();
		this.ignoreErrors("processor", () -> this.putProcessor(hardwareAbstractionLayer.getProcessor()));
		this.ignoreErrors("graphics", () -> this.putGraphics(hardwareAbstractionLayer.getGraphicsCards()));
		this.ignoreErrors("memory", () -> this.putMemory(hardwareAbstractionLayer.getMemory()));
	}

	private void ignoreErrors(String string, Runnable runnable) {
		try {
			runnable.run();
		} catch (Throwable var4) {
			LOGGER.warn("Failed retrieving info for group {}", string, var4);
		}
	}

	private void putPhysicalMemory(List<PhysicalMemory> list) {
		int i = 0;

		for (PhysicalMemory physicalMemory : list) {
			String string = String.format("Memory slot #%d ", i++);
			this.setDetail(string + "capacity (MB)", (Supplier<String>)(() -> String.format("%.2f", (float)physicalMemory.getCapacity() / 1048576.0F)));
			this.setDetail(string + "clockSpeed (GHz)", (Supplier<String>)(() -> String.format("%.2f", (float)physicalMemory.getClockSpeed() / 1.0E9F)));
			this.setDetail(string + "type", physicalMemory::getMemoryType);
		}
	}

	private void putVirtualMemory(VirtualMemory virtualMemory) {
		this.setDetail("Virtual memory max (MB)", (Supplier<String>)(() -> String.format("%.2f", (float)virtualMemory.getVirtualMax() / 1048576.0F)));
		this.setDetail("Virtual memory used (MB)", (Supplier<String>)(() -> String.format("%.2f", (float)virtualMemory.getVirtualInUse() / 1048576.0F)));
		this.setDetail("Swap memory total (MB)", (Supplier<String>)(() -> String.format("%.2f", (float)virtualMemory.getSwapTotal() / 1048576.0F)));
		this.setDetail("Swap memory used (MB)", (Supplier<String>)(() -> String.format("%.2f", (float)virtualMemory.getSwapUsed() / 1048576.0F)));
	}

	private void putMemory(GlobalMemory globalMemory) {
		this.ignoreErrors("physical memory", () -> this.putPhysicalMemory(globalMemory.getPhysicalMemory()));
		this.ignoreErrors("virtual memory", () -> this.putVirtualMemory(globalMemory.getVirtualMemory()));
	}

	private void putGraphics(List<GraphicsCard> list) {
		int i = 0;

		for (GraphicsCard graphicsCard : list) {
			String string = String.format("Graphics card #%d ", i++);
			this.setDetail(string + "name", graphicsCard::getName);
			this.setDetail(string + "vendor", graphicsCard::getVendor);
			this.setDetail(string + "VRAM (MB)", (Supplier<String>)(() -> String.format("%.2f", (float)graphicsCard.getVRam() / 1048576.0F)));
			this.setDetail(string + "deviceId", graphicsCard::getDeviceId);
			this.setDetail(string + "versionInfo", graphicsCard::getVersionInfo);
		}
	}

	private void putProcessor(CentralProcessor centralProcessor) {
		ProcessorIdentifier processorIdentifier = centralProcessor.getProcessorIdentifier();
		this.setDetail("Processor Vendor", processorIdentifier::getVendor);
		this.setDetail("Processor Name", processorIdentifier::getName);
		this.setDetail("Identifier", processorIdentifier::getIdentifier);
		this.setDetail("Microarchitecture", processorIdentifier::getMicroarchitecture);
		this.setDetail("Frequency (GHz)", (Supplier<String>)(() -> String.format("%.2f", (float)processorIdentifier.getVendorFreq() / 1.0E9F)));
		this.setDetail("Number of physical packages", (Supplier<String>)(() -> String.valueOf(centralProcessor.getPhysicalPackageCount())));
		this.setDetail("Number of physical CPUs", (Supplier<String>)(() -> String.valueOf(centralProcessor.getPhysicalProcessorCount())));
		this.setDetail("Number of logical CPUs", (Supplier<String>)(() -> String.valueOf(centralProcessor.getLogicalProcessorCount())));
	}

	public void appendToCrashReportString(StringBuilder stringBuilder) {
		stringBuilder.append("-- ").append("System Details").append(" --\n");
		stringBuilder.append("Details:");
		this.entries.forEach((string, string2) -> {
			stringBuilder.append("\n\t");
			stringBuilder.append(string);
			stringBuilder.append(": ");
			stringBuilder.append(string2);
		});
	}

	public String toLineSeparatedString() {
		return (String)this.entries
			.entrySet()
			.stream()
			.map(entry -> (String)entry.getKey() + ": " + (String)entry.getValue())
			.collect(Collectors.joining(System.lineSeparator()));
	}
}
