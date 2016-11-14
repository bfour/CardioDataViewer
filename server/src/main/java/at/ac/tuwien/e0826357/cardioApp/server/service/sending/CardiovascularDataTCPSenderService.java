package at.ac.tuwien.e0826357.cardioapp.server.service.sending;

import java.util.List;

import at.ac.tuwien.e0826357.cardioapp.commons.domain.CardiovascularData;
import at.ac.tuwien.e0826357.cardioapp.commons.service.CardiovascularDataMarshaller;
import at.ac.tuwien.e0826357.cardioapp.commons.service.ServiceException;

public class CardiovascularDataTCPSenderService implements
		SenderService<CardiovascularData> {

	private TCPSenderService tcpServ;

	public CardiovascularDataTCPSenderService(int port) throws ServiceException {
		this.tcpServ = new TCPSenderService(port);
	}

	@Override
	public void tell(CardiovascularData message) {
		StringBuilder builder = new StringBuilder();
		CardiovascularDataMarshaller.marshal(builder, message);
		tcpServ.tell(builder.toString());
	}

	@Override
	public void batchTell(List<CardiovascularData> message) {
		StringBuilder builder = new StringBuilder();
		for (CardiovascularData cardioData : message)
			CardiovascularDataMarshaller.marshal(builder, cardioData);
		tcpServ.tell(builder.toString());
	}

}
