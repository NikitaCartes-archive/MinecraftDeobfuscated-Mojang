package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;

public abstract class BaseComponent implements MutableComponent {
	protected final List<Component> siblings = Lists.<Component>newArrayList();
	private Style style = Style.EMPTY;

	@Override
	public MutableComponent append(Component component) {
		this.siblings.add(component);
		return this;
	}

	@Override
	public String getContents() {
		return "";
	}

	@Override
	public List<Component> getSiblings() {
		return this.siblings;
	}

	@Override
	public MutableComponent setStyle(Style style) {
		this.style = style;
		return this;
	}

	@Override
	public Style getStyle() {
		return this.style;
	}

	public abstract BaseComponent toMutable();

	@Override
	public final MutableComponent mutableCopy() {
		BaseComponent baseComponent = this.toMutable();
		baseComponent.siblings.addAll(this.siblings);
		baseComponent.setStyle(this.style);
		return baseComponent;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof BaseComponent)) {
			return false;
		} else {
			BaseComponent baseComponent = (BaseComponent)object;
			return this.siblings.equals(baseComponent.siblings) && Objects.equals(this.getStyle(), baseComponent.getStyle());
		}
	}

	public int hashCode() {
		return Objects.hash(new Object[]{this.getStyle(), this.siblings});
	}

	public String toString() {
		return "BaseComponent{style=" + this.style + ", siblings=" + this.siblings + '}';
	}
}
