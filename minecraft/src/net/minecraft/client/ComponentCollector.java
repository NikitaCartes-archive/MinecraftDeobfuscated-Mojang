package net.minecraft.client;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

@Environment(EnvType.CLIENT)
public class ComponentCollector {
	private boolean singleComponent = true;
	@Nullable
	private MutableComponent collector;

	public void append(MutableComponent mutableComponent) {
		if (this.collector == null) {
			this.collector = mutableComponent;
		} else {
			if (this.singleComponent) {
				this.collector = new TextComponent("").append(this.collector);
				this.singleComponent = false;
			}

			this.collector.append(mutableComponent);
		}
	}

	@Nullable
	public MutableComponent getResult() {
		return this.collector;
	}

	public MutableComponent getResultOrEmpty() {
		return (MutableComponent)(this.collector != null ? this.collector : new TextComponent(""));
	}
}
