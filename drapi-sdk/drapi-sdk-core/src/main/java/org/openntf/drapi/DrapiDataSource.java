package org.openntf.drapi;

import org.openntf.drapi.api.DocumentsApi;

/**
 * This class represents an API context on DRAPI. It is the bridge between DrapiClient and the API functions.
 */
public interface DrapiDataSource {

    /**
     * The name of the data source representation. Also known as the "scope" or "apiName" in some contexts. This is the name that will
     * be used to identify the data source in the API.
     *
     * @return the name of the scope representation
     */
    String dataSource();

    /**
     * Provides access to the Documents API for this data source. This allows for operations related to documents within the specified
     * data source.
     *
     * @return an instance of DocumentsApi for interacting with documents in this data source
     */
    DocumentsApi documents();

}
