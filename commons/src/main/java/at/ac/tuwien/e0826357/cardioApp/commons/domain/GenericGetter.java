package at.ac.tuwien.e0826357.cardioapp.commons.domain;

public abstract class GenericGetter<I,O> {
	public abstract O get(I obj);
}
