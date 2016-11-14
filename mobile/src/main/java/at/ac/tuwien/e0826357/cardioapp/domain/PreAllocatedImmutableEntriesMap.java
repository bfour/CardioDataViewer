/*
 * Copyright 2016 Florian Pollak (fpdevelop@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package at.ac.tuwien.e0826357.cardioapp.domain;

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
	public Set<Entry<K, V>> entrySet() {
		return entrySet;
	}

}
