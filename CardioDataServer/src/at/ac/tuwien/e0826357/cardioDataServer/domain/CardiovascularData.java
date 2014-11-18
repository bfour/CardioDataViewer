package at.ac.tuwien.e0826357.cardioDataServer.domain;

public class CardiovascularData {

	private long time;
	private int ECGA;
	private int ECGB;
	private int ECGC;
	private int oxygenSaturationPerMille;
	
	public CardiovascularData(long time, int eCGA, int eCGB, int eCGC,
			int oxygenSaturationPerMille) {
		this.time = time;
		ECGA = eCGA;
		ECGB = eCGB;
		ECGC = eCGC;
		this.oxygenSaturationPerMille = oxygenSaturationPerMille;
	}
	
	public long getTime() {
		return time;
	}
	public int getECGA() {
		return ECGA;
	}
	public int getECGB() {
		return ECGB;
	}
	public int getECGC() {
		return ECGC;
	}
	public int getOxygenSaturationPerMille() {
		return oxygenSaturationPerMille;
	}
	
}
