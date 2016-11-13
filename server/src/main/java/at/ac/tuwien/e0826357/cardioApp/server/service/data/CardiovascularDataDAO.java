package at.ac.tuwien.e0826357.cardioApp.server.service.data;

import java.util.List;

import at.ac.tuwien.e0826357.cardioApp.commons.domain.CardiovascularData;

public interface CardiovascularDataDAO {

	List<CardiovascularData> getAllAfter(long time) throws DataLayerException;

}
