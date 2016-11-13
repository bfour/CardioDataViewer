package at.ac.tuwien.e0826357.cardioapp.domain;

public class ServerLocationSpecification {

	private String uri;
	private int port;
	
	public ServerLocationSpecification(String uri, int port) {
		super();
		this.uri = uri;
		this.port = port;
	}

	protected String getUri() {
		return uri;
	}

	protected int getPort() {
		return port;
	}

}
