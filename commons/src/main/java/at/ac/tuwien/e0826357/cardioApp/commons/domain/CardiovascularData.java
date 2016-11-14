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

package at.ac.tuwien.e0826357.cardioapp.commons.domain;

public class CardiovascularData {

	private long time;
	private int leadI;
	private int leadII;
	private int leadIII;
	private int oxygenSaturationPerMille;
	
	public CardiovascularData(long time, int leadI, int leadII, int leadIII,
			int oxygenSaturationPerMille) {
		this.time = time;
		this.leadI = leadI;
		this.leadII = leadII;
		this.leadIII = leadIII;
		this.oxygenSaturationPerMille = oxygenSaturationPerMille;
	}
	
	public long getTime() {
		return time;
	}
	
	public int getLeadI() {
		return leadI;
	}

	public int getLeadII() {
		return leadII;
	}

	public int getLeadIII() {
		return leadIII;
	}

	public int getOxygenSaturationPerMille() {
		return oxygenSaturationPerMille;
	}

	@Override
	public String toString() {
		return "CardiovascularData [time=" + time + ", leadI=" + leadI
				+ ", leadII=" + leadII + ", leadIII=" + leadIII
				+ ", oxygenSaturationPerMille=" + oxygenSaturationPerMille
				+ "]";
	}
	
}
