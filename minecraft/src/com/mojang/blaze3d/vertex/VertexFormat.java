package com.mojang.blaze3d.vertex;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class VertexFormat {
	private static final Logger LOGGER = LogManager.getLogger();
	private final List<VertexFormatElement> elements = Lists.<VertexFormatElement>newArrayList();
	private final List<Integer> offsets = Lists.<Integer>newArrayList();
	private int vertexSize;
	private int colorOffset = -1;
	private final List<Integer> texOffset = Lists.<Integer>newArrayList();
	private int normalOffset = -1;

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
		this.colorOffset = -1;
		this.texOffset.clear();
		this.normalOffset = -1;
		this.vertexSize = 0;
	}

	public VertexFormat addElement(VertexFormatElement vertexFormatElement) {
		if (vertexFormatElement.isPosition() && this.hasPositionElement()) {
			LOGGER.warn("VertexFormat error: Trying to add a position VertexFormatElement when one already exists, ignoring.");
			return this;
		} else {
			this.elements.add(vertexFormatElement);
			this.offsets.add(this.vertexSize);
			switch (vertexFormatElement.getUsage()) {
				case NORMAL:
					this.normalOffset = this.vertexSize;
					break;
				case COLOR:
					this.colorOffset = this.vertexSize;
					break;
				case UV:
					this.texOffset.add(vertexFormatElement.getIndex(), this.vertexSize);
			}

			this.vertexSize = this.vertexSize + vertexFormatElement.getByteSize();
			return this;
		}
	}

	public boolean hasNormal() {
		return this.normalOffset >= 0;
	}

	public int getNormalOffset() {
		return this.normalOffset;
	}

	public boolean hasColor() {
		return this.colorOffset >= 0;
	}

	public int getColorOffset() {
		return this.colorOffset;
	}

	public boolean hasUv(int i) {
		return this.texOffset.size() - 1 >= i;
	}

	public int getUvOffset(int i) {
		return (Integer)this.texOffset.get(i);
	}

	public String toString() {
		String string = "format: " + this.elements.size() + " elements: ";

		for (int i = 0; i < this.elements.size(); i++) {
			string = string + ((VertexFormatElement)this.elements.get(i)).toString();
			if (i != this.elements.size() - 1) {
				string = string + " ";
			}
		}

		return string;
	}

	private boolean hasPositionElement() {
		int i = 0;

		for (int j = this.elements.size(); i < j; i++) {
			VertexFormatElement vertexFormatElement = (VertexFormatElement)this.elements.get(i);
			if (vertexFormatElement.isPosition()) {
				return true;
			}
		}

		return false;
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

	public int getOffset(int i) {
		return (Integer)this.offsets.get(i);
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
}
