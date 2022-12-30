package org.gottschd.stax.processors;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;


public class StaxParserPath {

	private final LinkedList<String> pathElements;

	public StaxParserPath() {
		this.pathElements = new LinkedList<>();
	}

	public StaxParserPath(Queue<String> path) {
		this.pathElements = new LinkedList<>(path);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof StaxParserPath other)) {
			return false;
		}

		return Objects.equals(pathElements, other.pathElements);
	}

	@Override
	public int hashCode() {
		return Objects.hash(pathElements);
	}

	@Override
	public String toString() {
		return "StaxParsePath{" + "path=" + pathElements + '}';
	}

	public void addLast(String localName) {
		pathElements.addLast(localName);
	}

	public void removeLast() {
		pathElements.removeLast();
	}

	public static StaxParserPath fromString(String localTagPath) {
		return new StaxParserPath(
				Arrays.stream(localTagPath.split("/")).collect(Collectors.toCollection(LinkedList::new)));
	}

	public boolean startWith(StaxParserPath path) {
		return CollectionUtils.isSubCollection(path.pathElements, this.pathElements);
	}

}
