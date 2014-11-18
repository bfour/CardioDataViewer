package at.ac.tuwien.e0826357.cardioDataServer.service.sending;

import java.util.List;

public interface SenderService<T> {

	void tell(T message);
	void batchTell(List<T> message);
	
}
