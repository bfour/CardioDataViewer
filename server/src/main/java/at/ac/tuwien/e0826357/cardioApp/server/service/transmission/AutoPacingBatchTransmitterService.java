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

package at.ac.tuwien.e0826357.cardioapp.server.service.transmission;

import at.ac.tuwien.e0826357.cardioapp.server.service.data.DataService;
import at.ac.tuwien.e0826357.cardioapp.server.service.sending.SenderService;

public class AutoPacingBatchTransmitterService<T> extends TransmitterService<T> {

	private int batchTargetSize;

	public AutoPacingBatchTransmitterService(DataService<T> dataServ,
											 SenderService<T> senderServ, int batchTargetSize) {
		super(dataServ, senderServ);
		this.batchTargetSize = batchTargetSize;
	}

	@Override
	public Runnable getTransmitter() {
		return new AutoPacingBatchTransmitter<>(getDataServ(),
				getSenderServ(), batchTargetSize);
	}

}
