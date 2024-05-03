package net.minecraft;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
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
	private static final Logger LOGGER = LogUtils.getLogger();
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
			return String.format(Locale.ROOT, "%d total; %s", list.size(), String.join(" ", list));
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
		this.ignoreErrors("storage", this::putStorage);
	}

	private void ignoreErrors(String string, Runnable runnable) {
		try {
			runnable.run();
		} catch (Throwable var4) {
			LOGGER.warn("Failed retrieving info for group {}", string, var4);
		}
	}

	public static float sizeInMiB(long l) {
		return (float)l / 1048576.0F;
	}

	private void putPhysicalMemory(List<PhysicalMemory> list) {
		int i = 0;

		for (PhysicalMemory physicalMemory : list) {
			String string = String.format(Locale.ROOT, "Memory slot #%d ", i++);
			this.setDetail(string + "capacity (MiB)", (Supplier<String>)(() -> String.format(Locale.ROOT, "%.2f", sizeInMiB(physicalMemory.getCapacity()))));
			this.setDetail(string + "clockSpeed (GHz)", (Supplier<String>)(() -> String.format(Locale.ROOT, "%.2f", (float)physicalMemory.getClockSpeed() / 1.0E9F)));
			this.setDetail(string + "type", physicalMemory::getMemoryType);
		}
	}

	private void putVirtualMemory(VirtualMemory virtualMemory) {
		this.setDetail("Virtual memory max (MiB)", (Supplier<String>)(() -> String.format(Locale.ROOT, "%.2f", sizeInMiB(virtualMemory.getVirtualMax()))));
		this.setDetail("Virtual memory used (MiB)", (Supplier<String>)(() -> String.format(Locale.ROOT, "%.2f", sizeInMiB(virtualMemory.getVirtualInUse()))));
		this.setDetail("Swap memory total (MiB)", (Supplier<String>)(() -> String.format(Locale.ROOT, "%.2f", sizeInMiB(virtualMemory.getSwapTotal()))));
		this.setDetail("Swap memory used (MiB)", (Supplier<String>)(() -> String.format(Locale.ROOT, "%.2f", sizeInMiB(virtualMemory.getSwapUsed()))));
	}

	private void putMemory(GlobalMemory globalMemory) {
		this.ignoreErrors("physical memory", () -> this.putPhysicalMemory(globalMemory.getPhysicalMemory()));
		this.ignoreErrors("virtual memory", () -> this.putVirtualMemory(globalMemory.getVirtualMemory()));
	}

	private void putGraphics(List<GraphicsCard> list) {
		int i = 0;

		for (GraphicsCard graphicsCard : list) {
			String string = String.format(Locale.ROOT, "Graphics card #%d ", i++);
			this.setDetail(string + "name", graphicsCard::getName);
			this.setDetail(string + "vendor", graphicsCard::getVendor);
			this.setDetail(string + "VRAM (MiB)", (Supplier<String>)(() -> String.format(Locale.ROOT, "%.2f", sizeInMiB(graphicsCard.getVRam()))));
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
		this.setDetail("Frequency (GHz)", (Supplier<String>)(() -> String.format(Locale.ROOT, "%.2f", (float)processorIdentifier.getVendorFreq() / 1.0E9F)));
		this.setDetail("Number of physical packages", (Supplier<String>)(() -> String.valueOf(centralProcessor.getPhysicalPackageCount())));
		this.setDetail("Number of physical CPUs", (Supplier<String>)(() -> String.valueOf(centralProcessor.getPhysicalProcessorCount())));
		this.setDetail("Number of logical CPUs", (Supplier<String>)(() -> String.valueOf(centralProcessor.getLogicalProcessorCount())));
	}

	private void putStorage() {
		this.putSpaceForProperty("jna.tmpdir");
		this.putSpaceForProperty("org.lwjgl.system.SharedLibraryExtractPath");
		this.putSpaceForProperty("io.netty.native.workdir");
		this.putSpaceForProperty("java.io.tmpdir");
		this.putSpaceForPath("workdir", () -> "");
	}

	private void putSpaceForProperty(String string) {
		this.putSpaceForPath(string, () -> System.getProperty(string));
	}

	private void putSpaceForPath(String string, Supplier<String> supplier) {
		String string2 = "Space in storage for " + string + " (MiB)";

		try {
			String string3 = (String)supplier.get();
			if (string3 == null) {
				this.setDetail(string2, "<path not set>");
				return;
			}

			FileStore fileStore = Files.getFileStore(Path.of(string3));
			this.setDetail(
				string2, String.format(Locale.ROOT, "available: %.2f, total: %.2f", sizeInMiB(fileStore.getUsableSpace()), sizeInMiB(fileStore.getTotalSpace()))
			);
		} catch (InvalidPathException var6) {
			LOGGER.warn("{} is not a path", string, var6);
			this.setDetail(string2, "<invalid path>");
		} catch (Exception var7) {
			LOGGER.warn("Failed retrieving storage space for {}", string, var7);
			this.setDetail(string2, "ERR");
		}
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
