package at.ac.tuwien.e0826357.cardioDataCommons.utils;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.e0826357.cardioDataCommons.domain.Triple;
import at.ac.tuwien.e0826357.cardioDataCommons.domain.Tuple;

public class Utils {

	public static <A, B, C> List<Triple<A, B, C>> mapTuples(
			List<Tuple<A, B>> aAndBs, List<C> cs)
			throws InvalidParameterException {

		if (aAndBs.size() != cs.size())
			throw new InvalidParameterException(
					"tuple list must have same size "
							+ "as list of objects to be mapped to tuple list");

		List<Triple<A, B, C>> triples = new ArrayList<>(aAndBs.size());
		for (int index = 0; index < aAndBs.size(); index++) {
			triples.add(new Triple<A, B, C>(aAndBs.get(index).getA(), aAndBs
					.get(index).getB(), cs.get(index)));
		}
		return triples;

	}
}
