package org.openntf.drapi.internal.api;

import org.openntf.drapi.DrapiContext;

public abstract class AbstractDataSourceApi extends AbstractApi {

    protected static final String QS_DATASOURCE = "dataSource";

    private final String dataSource;

    protected AbstractDataSourceApi(DrapiContext context, String dataSource) {
        super(context);
        this.dataSource = dataSource;
    }

    protected String dataSource() {
        return dataSource;
    }

}
