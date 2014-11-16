package at.ac.tuwien.e0826357.cardioDataViewer.domain;

import java.util.AbstractMap;
import java.util.Set;

public class PreAllocatedImmutableEntriesMap<K, V> extends AbstractMap<K, V> {

	private Set<Entry<K, V>> entrySet;

	public PreAllocatedImmutableEntriesMap(int numberOfEntries) {
		this.entrySet = new PreAllocatedDuplicateAllowingSet<Entry<K, V>>(
				numberOfEntries);
	}

	@Override
	public V put(K key, V value) {
		entrySet.add(new SimpleImmutableEntry<K, V>(key, value));
		return null; // TODO document non-compliance with javadoc specification
						// for performance reasons
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return entrySet;
	}

}
