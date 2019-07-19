package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class BaseComponent implements Component {
	protected final List<Component> siblings = Lists.<Component>newArrayList();
	private Style style;

	@Override
	public Component append(Component component) {
		component.getStyle().inheritFrom(this.getStyle());
		this.siblings.add(component);
		return this;
	}

	@Override
	public List<Component> getSiblings() {
		return this.siblings;
	}

	@Override
	public Component setStyle(Style style) {
		this.style = style;

		for (Component component : this.siblings) {
			component.getStyle().inheritFrom(this.getStyle());
		}

		return this;
	}

	@Override
	public Style getStyle() {
		if (this.style == null) {
			this.style = new Style();

			for (Component component : this.siblings) {
				component.getStyle().inheritFrom(this.style);
			}
		}

		return this.style;
	}

	@Override
	public Stream<Component> stream() {
		return Streams.concat(Stream.of(this), this.siblings.stream().flatMap(Component::stream));
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof BaseComponent)) {
			return false;
		} else {
			BaseComponent baseComponent = (BaseComponent)object;
			return this.siblings.equals(baseComponent.siblings) && this.getStyle().equals(baseComponent.getStyle());
		}
	}

	public int hashCode() {
		return Objects.hash(new Object[]{this.getStyle(), this.siblings});
	}

	public String toString() {
		return "BaseComponent{style=" + this.style + ", siblings=" + this.siblings + '}';
	}
}
