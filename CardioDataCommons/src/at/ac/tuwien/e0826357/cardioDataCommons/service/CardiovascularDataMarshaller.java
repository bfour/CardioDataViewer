package at.ac.tuwien.e0826357.cardioDataCommons.service;

import java.util.StringTokenizer;

import at.ac.tuwien.e0826357.cardioDataCommons.domain.CardiovascularData;

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
		strBuilder.append(cardioData.getECGA());
		strBuilder.append(";");
		strBuilder.append(cardioData.getECGB());
		strBuilder.append(";");
		strBuilder.append(cardioData.getECGC());
		strBuilder.append(";");
		strBuilder.append(cardioData.getOxygenSaturationPerMille());
		strBuilder.append("\n");
	}

	public static CardiovascularData unmarshal(String line) {
		StringTokenizer tokenizer = new StringTokenizer(line, ";");
		long time = Long.parseLong(tokenizer.nextToken());
		int ECGA = Integer.parseInt(tokenizer.nextToken());
		int ECGB = Integer.parseInt(tokenizer.nextToken());
		int ECGC = Integer.parseInt(tokenizer.nextToken());
		int oxygenSaturationPerMille = Integer.parseInt(tokenizer.nextToken());
		return new CardiovascularData(time, ECGA, ECGB, ECGC,
				oxygenSaturationPerMille);
	}

}
