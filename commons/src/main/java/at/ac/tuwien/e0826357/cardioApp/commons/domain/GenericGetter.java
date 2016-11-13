package at.ac.tuwien.e0826357.cardioApp.commons.domain;

public abstract class GenericGetter<I,O> {
	public abstract O get(I obj);
}
