package net.minecraft.world;

import com.google.common.collect.Maps;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class Snooper {
	private final Map<String, Object> fixedData = Maps.<String, Object>newHashMap();
	private final Map<String, Object> dynamicData = Maps.<String, Object>newHashMap();
	private final String token = UUID.randomUUID().toString();
	private final URL url;
	private final SnooperPopulator populator;
	private final Timer timer = new Timer("Snooper Timer", true);
	private final Object lock = new Object();
	private final long startupTime;
	private boolean started;

	public Snooper(String string, SnooperPopulator snooperPopulator, long l) {
		try {
			this.url = new URL("http://snoop.minecraft.net/" + string + "?version=" + 2);
		} catch (MalformedURLException var6) {
			throw new IllegalArgumentException();
		}

		this.populator = snooperPopulator;
		this.startupTime = l;
	}

	public void start() {
		if (!this.started) {
		}
	}

	public void prepare() {
		this.setFixedData("memory_total", Runtime.getRuntime().totalMemory());
		this.setFixedData("memory_max", Runtime.getRuntime().maxMemory());
		this.setFixedData("memory_free", Runtime.getRuntime().freeMemory());
		this.setFixedData("cpu_cores", Runtime.getRuntime().availableProcessors());
		this.populator.populateSnooper(this);
	}

	public void setDynamicData(String string, Object object) {
		synchronized (this.lock) {
			this.dynamicData.put(string, object);
		}
	}

	public void setFixedData(String string, Object object) {
		synchronized (this.lock) {
			this.fixedData.put(string, object);
		}
	}

	public boolean isStarted() {
		return this.started;
	}

	public void interrupt() {
		this.timer.cancel();
	}

	@Environment(EnvType.CLIENT)
	public String getToken() {
		return this.token;
	}

	public long getStartupTime() {
		return this.startupTime;
	}
}
