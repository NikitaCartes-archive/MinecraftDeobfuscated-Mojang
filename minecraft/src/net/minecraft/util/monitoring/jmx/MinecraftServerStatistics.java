package net.minecraft.util.monitoring.jmx;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MinecraftServerStatistics implements DynamicMBean {
	private static final Logger LOGGER = LogManager.getLogger();
	private final MinecraftServer server;
	private final MBeanInfo mBeanInfo;
	private final Map<String, MinecraftServerStatistics.AttributeDescription> attributeDescriptionByName = (Map<String, MinecraftServerStatistics.AttributeDescription>)Stream.of(
			new MinecraftServerStatistics.AttributeDescription("tickTimes", this::getTickTimes, "Historical tick times (ms)", long[].class),
			new MinecraftServerStatistics.AttributeDescription("averageTickTime", this::getAverageTickTime, "Current average tick time (ms)", long.class)
		)
		.collect(Collectors.toMap(attributeDescription -> attributeDescription.name, Function.identity()));

	private MinecraftServerStatistics(MinecraftServer minecraftServer) {
		this.server = minecraftServer;
		MBeanAttributeInfo[] mBeanAttributeInfos = (MBeanAttributeInfo[])this.attributeDescriptionByName
			.values()
			.stream()
			.map(MinecraftServerStatistics.AttributeDescription::asMBeanAttributeInfo)
			.toArray(MBeanAttributeInfo[]::new);
		this.mBeanInfo = new MBeanInfo(
			MinecraftServerStatistics.class.getSimpleName(), "metrics for dedicated server", mBeanAttributeInfos, null, null, new MBeanNotificationInfo[0]
		);
	}

	public static void registerJmxMonitoring(MinecraftServer minecraftServer) {
		try {
			ManagementFactory.getPlatformMBeanServer().registerMBean(new MinecraftServerStatistics(minecraftServer), new ObjectName("net.minecraft.server:type=Server"));
		} catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException | MalformedObjectNameException var2) {
			LOGGER.warn("Failed to initialise server as JMX bean", (Throwable)var2);
		}
	}

	private float getAverageTickTime() {
		return this.server.getAverageTickTime();
	}

	private long[] getTickTimes() {
		return this.server.tickTimes;
	}

	@Nullable
	public Object getAttribute(String string) {
		MinecraftServerStatistics.AttributeDescription attributeDescription = (MinecraftServerStatistics.AttributeDescription)this.attributeDescriptionByName
			.get(string);
		return attributeDescription == null ? null : attributeDescription.getter.get();
	}

	public void setAttribute(Attribute attribute) {
	}

	public AttributeList getAttributes(String[] strings) {
		List<Attribute> list = (List<Attribute>)Arrays.stream(strings)
			.map(this.attributeDescriptionByName::get)
			.filter(Objects::nonNull)
			.map(attributeDescription -> new Attribute(attributeDescription.name, attributeDescription.getter.get()))
			.collect(Collectors.toList());
		return new AttributeList(list);
	}

	public AttributeList setAttributes(AttributeList attributeList) {
		return new AttributeList();
	}

	@Nullable
	public Object invoke(String string, Object[] objects, String[] strings) {
		return null;
	}

	public MBeanInfo getMBeanInfo() {
		return this.mBeanInfo;
	}

	static final class AttributeDescription {
		final String name;
		final Supplier<Object> getter;
		private final String description;
		private final Class<?> type;

		AttributeDescription(String string, Supplier<Object> supplier, String string2, Class<?> class_) {
			this.name = string;
			this.getter = supplier;
			this.description = string2;
			this.type = class_;
		}

		private MBeanAttributeInfo asMBeanAttributeInfo() {
			return new MBeanAttributeInfo(this.name, this.type.getSimpleName(), this.description, true, false, false);
		}
	}
}
