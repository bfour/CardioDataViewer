package at.ac.tuwien.e0826357.cardioApp.server.service.data;

public class DataLayerException extends Exception {

	private static final long serialVersionUID = 4480832390444683913L;

	public DataLayerException() {
	}

	public DataLayerException(String arg0) {
		super(arg0);
	}

	public DataLayerException(Throwable arg0) {
		super(arg0);
	}

	public DataLayerException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public DataLayerException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1);
		// super(arg0, arg1, arg2, arg3); // requires min Android v24
	}

}
