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

package at.ac.tuwien.e0826357.cardioapp.commons.service;

import java.util.StringTokenizer;

import at.ac.tuwien.e0826357.cardioapp.commons.domain.CardiovascularData;

public class CardiovascularDataMarshaller {

	// private static CardiovascularDataMarshaller instance;

	private CardiovascularDataMarshaller() {
	}

	// public static CardiovascularDataMarshaller getInstance() {
	// if (instance == null) instance = new CardiovascularDataMarshaller();
	// return instance;
	// }

	public static void marshal(StringBuilder strBuilder,
			CardiovascularData cardioData) {
		strBuilder.append(cardioData.getTime());
		strBuilder.append(";");
		strBuilder.append(cardioData.getLeadI());
		strBuilder.append(";");
		strBuilder.append(cardioData.getLeadII());
		strBuilder.append(";");
		strBuilder.append(cardioData.getLeadIII());
		strBuilder.append(";");
		strBuilder.append(cardioData.getOxygenSaturationPerMille());
		strBuilder.append("\n");
	}

	public static CardiovascularData unmarshal(String line) {
		StringTokenizer tokenizer = new StringTokenizer(line, ";");
		long time = Long.parseLong(tokenizer.nextToken());
		int leadI = Integer.parseInt(tokenizer.nextToken());
		int leadII = Integer.parseInt(tokenizer.nextToken());
		int leadIII = Integer.parseInt(tokenizer.nextToken());
		int oxygenSaturationPerMille = Integer.parseInt(tokenizer.nextToken());
		return new CardiovascularData(time, leadI, leadII, leadIII,
				oxygenSaturationPerMille);
	}

}
