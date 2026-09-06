package org.openntf.drapi.internal;

import org.openntf.drapi.DrapiContext;
import org.openntf.drapi.DrapiDataSource;
import org.openntf.drapi.api.DocumentsApi;
import org.openntf.drapi.internal.api.DocumentsApiImpl;

public class DrapiDataSourceImpl implements DrapiDataSource {

    private final String dataSource;
    private final DrapiContext context;

    DrapiDataSourceImpl(String dataSource, DrapiContext context) {
        this.dataSource = dataSource;
        this.context = context;
    }

    @Override
    public String dataSource() {
        return dataSource;
    }

    @Override
    public DocumentsApi documents() {
        return new DocumentsApiImpl(context, dataSource);
    }


}
