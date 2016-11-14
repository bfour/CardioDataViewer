package at.ac.tuwien.e0826357.cardioapp.server.service.data;

import java.util.List;

import at.ac.tuwien.e0826357.cardioapp.commons.domain.CardiovascularData;

public interface CardiovascularDataDAO {

	List<CardiovascularData> getAllAfter(long time) throws DataLayerException;

}
