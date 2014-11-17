package at.ac.tuwien.e0826357.cardioDataViewer.service;

public class ServiceException extends Exception {

	private static final long serialVersionUID = -4082534357162483026L;

	public ServiceException() {
	}

	public ServiceException(String detailMessage) {
		super(detailMessage);
	}

	public ServiceException(Throwable throwable) {
		super(throwable);
	}

	public ServiceException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
