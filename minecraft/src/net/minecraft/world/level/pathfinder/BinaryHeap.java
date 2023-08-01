package net.minecraft.world.level.pathfinder;

import java.util.Arrays;

public class BinaryHeap {
	private Node[] heap = new Node[128];
	private int size;

	public Node insert(Node node) {
		if (node.heapIdx >= 0) {
			throw new IllegalStateException("OW KNOWS!");
		} else {
			if (this.size == this.heap.length) {
				Node[] nodes = new Node[this.size << 1];
				System.arraycopy(this.heap, 0, nodes, 0, this.size);
				this.heap = nodes;
			}

			this.heap[this.size] = node;
			node.heapIdx = this.size;
			this.upHeap(this.size++);
			return node;
		}
	}

	public void clear() {
		this.size = 0;
	}

	public Node peek() {
		return this.heap[0];
	}

	public Node pop() {
		Node node = this.heap[0];
		this.heap[0] = this.heap[--this.size];
		this.heap[this.size] = null;
		if (this.size > 0) {
			this.downHeap(0);
		}

		node.heapIdx = -1;
		return node;
	}

	public void remove(Node node) {
		this.heap[node.heapIdx] = this.heap[--this.size];
		this.heap[this.size] = null;
		if (this.size > node.heapIdx) {
			if (this.heap[node.heapIdx].f < node.f) {
				this.upHeap(node.heapIdx);
			} else {
				this.downHeap(node.heapIdx);
			}
		}

		node.heapIdx = -1;
	}

	public void changeCost(Node node, float f) {
		float g = node.f;
		node.f = f;
		if (f < g) {
			this.upHeap(node.heapIdx);
		} else {
			this.downHeap(node.heapIdx);
		}
	}

	public int size() {
		return this.size;
	}

	private void upHeap(int i) {
		Node node = this.heap[i];
		float f = node.f;

		while (i > 0) {
			int j = i - 1 >> 1;
			Node node2 = this.heap[j];
			if (!(f < node2.f)) {
				break;
			}

			this.heap[i] = node2;
			node2.heapIdx = i;
			i = j;
		}

		this.heap[i] = node;
		node.heapIdx = i;
	}

	private void downHeap(int i) {
		Node node = this.heap[i];
		float f = node.f;

		while (true) {
			int j = 1 + (i << 1);
			int k = j + 1;
			if (j >= this.size) {
				break;
			}

			Node node2 = this.heap[j];
			float g = node2.f;
			Node node3;
			float h;
			if (k >= this.size) {
				node3 = null;
				h = Float.POSITIVE_INFINITY;
			} else {
				node3 = this.heap[k];
				h = node3.f;
			}

			if (g < h) {
				if (!(g < f)) {
					break;
				}

				this.heap[i] = node2;
				node2.heapIdx = i;
				i = j;
			} else {
				if (!(h < f)) {
					break;
				}

				this.heap[i] = node3;
				node3.heapIdx = i;
				i = k;
			}
		}

		this.heap[i] = node;
		node.heapIdx = i;
	}

	public boolean isEmpty() {
		return this.size == 0;
	}

	public Node[] getHeap() {
		return (Node[])Arrays.copyOf(this.heap, this.size);
	}
}
