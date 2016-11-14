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
