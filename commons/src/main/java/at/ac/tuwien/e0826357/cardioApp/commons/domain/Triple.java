package at.ac.tuwien.e0826357.cardioapp.commons.domain;


public class Triple<A,B,C> {
	
	private final A a;
	private final B b;
	private final C c;
	
	public Triple(A a, B b, C c) {
		super();
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public A getA() {
		return a;
	}

	public B getB() {
		return b;
	}

	public C getC() {
		return c;
	}
	
}
