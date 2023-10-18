package net.minecraft.client.renderer;

import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class SectionBufferBuilderPool {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final int MAX_BUILDERS_32_BIT = 4;
	private final Queue<SectionBufferBuilderPack> freeBuffers;
	private volatile int freeBufferCount;

	private SectionBufferBuilderPool(List<SectionBufferBuilderPack> list) {
		this.freeBuffers = Queues.<SectionBufferBuilderPack>newArrayDeque(list);
		this.freeBufferCount = this.freeBuffers.size();
	}

	public static SectionBufferBuilderPool allocate(int i) {
		int j = Math.max(1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3) / SectionBufferBuilderPack.TOTAL_BUFFERS_SIZE);
		int k = Math.max(1, Math.min(i, j));
		List<SectionBufferBuilderPack> list = new ArrayList(k);

		try {
			for (int l = 0; l < k; l++) {
				list.add(new SectionBufferBuilderPack());
			}
		} catch (OutOfMemoryError var7) {
			LOGGER.warn("Allocated only {}/{} buffers", list.size(), k);
			int m = Math.min(list.size() * 2 / 3, list.size() - 1);

			for (int n = 0; n < m; n++) {
				((SectionBufferBuilderPack)list.remove(list.size() - 1)).close();
			}
		}

		return new SectionBufferBuilderPool(list);
	}

	@Nullable
	public SectionBufferBuilderPack acquire() {
		SectionBufferBuilderPack sectionBufferBuilderPack = (SectionBufferBuilderPack)this.freeBuffers.poll();
		if (sectionBufferBuilderPack != null) {
			this.freeBufferCount = this.freeBuffers.size();
			return sectionBufferBuilderPack;
		} else {
			return null;
		}
	}

	public void release(SectionBufferBuilderPack sectionBufferBuilderPack) {
		this.freeBuffers.add(sectionBufferBuilderPack);
		this.freeBufferCount = this.freeBuffers.size();
	}

	public boolean isEmpty() {
		return this.freeBuffers.isEmpty();
	}

	public int getFreeBufferCount() {
		return this.freeBufferCount;
	}
}
