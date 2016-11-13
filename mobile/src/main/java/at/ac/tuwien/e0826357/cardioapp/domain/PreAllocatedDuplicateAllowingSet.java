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
