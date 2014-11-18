package at.ac.tuwien.e0826357.cardioDataServer.service.sending;

import java.util.List;

import at.ac.tuwien.e0826357.cardioDataServer.domain.CardiovascularData;
import at.ac.tuwien.e0826357.cardioDataViewer.service.ServiceException;

public class CardiovascularDataTCPSenderService implements SenderService<CardiovascularData> {

	private TCPSenderService tcpServ;
	
	public CardiovascularDataTCPSenderService(int port) throws ServiceException {
		this.tcpServ = new TCPSenderService(port);
	}

	@Override
	public void tell(CardiovascularData message) {
		StringBuilder builder = new StringBuilder();
		marshal(builder, message);
		tcpServ.tell(builder.toString());
	}

	@Override
	public void batchTell(List<CardiovascularData> message) {
		StringBuilder builder = new StringBuilder();
		for (CardiovascularData cardioData : message)
			marshal(builder, cardioData);
		tcpServ.tell(builder.toString());		
	}
	
	private void marshal(StringBuilder strBuilder, CardiovascularData cardioData) {
		strBuilder.append(cardioData.getTime());
		strBuilder.append(";");
		strBuilder.append(cardioData.getECGA());
		strBuilder.append(";");
		strBuilder.append(cardioData.getECGB());
		strBuilder.append(";");
		strBuilder.append(cardioData.getECGC());
		strBuilder.append(";");
		strBuilder.append(cardioData.getOxygenSaturationPerMille());
		strBuilder.append("\n");
	}

}
