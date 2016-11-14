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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class PreAllocatedDuplicateAllowingSet<E> extends AbstractSet<E> {

	private List<E> entries;
	
	public PreAllocatedDuplicateAllowingSet(int numberOfEntries) {
		entries = new ArrayList<E>(numberOfEntries);
	}
	
	@Override
	public boolean removeAll(Collection<?> collection) {
		return entries.removeAll(collection);
	}

	@Override
	public boolean add(E object) {
		return entries.add(object);
	}

	@Override
	public boolean addAll(Collection<? extends E> collection) {
		return entries.addAll(collection);
	}

	@Override
	public void clear() {
		entries.clear();
	}

	@Override
	public boolean contains(Object object) {
		return entries.contains(object);
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		return entries.containsAll(collection);
	}

	@Override
	public boolean isEmpty() {
		return entries.isEmpty();
	}

	@Override
	public boolean remove(Object object) {
		return entries.remove(object);
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		return entries.retainAll(collection);
	}

	@Override
	public Object[] toArray() {
		return entries.toArray();
	}

	@Override
	public <T> T[] toArray(T[] contents) {
		return entries.toArray(contents);
	}

	@Override
	public Iterator<E> iterator() {
		return entries.iterator();
	}

	@Override
	public int size() {
		return entries.size();
	}

}
