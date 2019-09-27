package com.mojang.blaze3d.vertex;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class VertexFormat {
	private static final Logger LOGGER = LogManager.getLogger();
	private final List<VertexFormatElement> elements = Lists.<VertexFormatElement>newArrayList();
	private final IntList offsets = new IntArrayList();
	private int vertexSize;

	public VertexFormat(VertexFormat vertexFormat) {
		this();

		for (int i = 0; i < vertexFormat.getElementCount(); i++) {
			this.addElement(vertexFormat.getElement(i));
		}

		this.vertexSize = vertexFormat.getVertexSize();
	}

	public VertexFormat() {
	}

	public void clear() {
		this.elements.clear();
		this.offsets.clear();
		this.vertexSize = 0;
	}

	public VertexFormat addElement(VertexFormatElement vertexFormatElement) {
		if (vertexFormatElement.isPosition() && this.hasPositionElement()) {
			LOGGER.warn("VertexFormat error: Trying to add a position VertexFormatElement when one already exists, ignoring.");
			return this;
		} else {
			this.elements.add(vertexFormatElement);
			this.offsets.add(this.vertexSize);
			this.vertexSize = this.vertexSize + vertexFormatElement.getByteSize();
			return this;
		}
	}

	public String toString() {
		return "format: " + this.elements.size() + " elements: " + (String)this.elements.stream().map(Object::toString).collect(Collectors.joining(" "));
	}

	private boolean hasPositionElement() {
		return this.elements.stream().anyMatch(VertexFormatElement::isPosition);
	}

	public int getIntegerSize() {
		return this.getVertexSize() / 4;
	}

	public int getVertexSize() {
		return this.vertexSize;
	}

	public List<VertexFormatElement> getElements() {
		return this.elements;
	}

	public int getElementCount() {
		return this.elements.size();
	}

	public VertexFormatElement getElement(int i) {
		return (VertexFormatElement)this.elements.get(i);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			VertexFormat vertexFormat = (VertexFormat)object;
			if (this.vertexSize != vertexFormat.vertexSize) {
				return false;
			} else {
				return !this.elements.equals(vertexFormat.elements) ? false : this.offsets.equals(vertexFormat.offsets);
			}
		} else {
			return false;
		}
	}

	public int hashCode() {
		int i = this.elements.hashCode();
		i = 31 * i + this.offsets.hashCode();
		return 31 * i + this.vertexSize;
	}

	public void setupBufferState(long l) {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> this.setupBufferState(l));
		} else {
			int i = this.getVertexSize();
			List<VertexFormatElement> list = this.getElements();

			for (int j = 0; j < list.size(); j++) {
				((VertexFormatElement)list.get(j)).setupBufferState(l + (long)this.offsets.getInt(j), i);
			}
		}
	}

	public void clearBufferState() {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(this::clearBufferState);
		} else {
			for (VertexFormatElement vertexFormatElement : this.getElements()) {
				vertexFormatElement.clearBufferState();
			}
		}
	}
}
