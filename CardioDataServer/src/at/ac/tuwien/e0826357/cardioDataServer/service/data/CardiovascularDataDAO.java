package at.ac.tuwien.e0826357.cardioDataServer.service.data;

import java.util.List;

import at.ac.tuwien.e0826357.cardioDataServer.domain.CardiovascularData;

public interface CardiovascularDataDAO {

	List<CardiovascularData> getAllAfter(long time) throws DataLayerException;

}
