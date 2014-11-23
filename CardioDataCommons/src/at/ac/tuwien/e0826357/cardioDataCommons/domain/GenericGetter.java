package at.ac.tuwien.e0826357.cardioDataCommons.domain;

public abstract class GenericGetter<I,O> {
	public abstract O get(I obj);
}
