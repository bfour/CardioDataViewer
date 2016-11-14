/*
 * Copyright 2016 Florian Pollak (fpdevelop@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
