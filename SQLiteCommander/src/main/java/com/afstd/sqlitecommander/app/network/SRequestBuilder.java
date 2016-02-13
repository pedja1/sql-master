package com.afstd.sqlitecommander.app.network;

import com.tehnicomsolutions.http.RequestBuilder;

/**
 * Created by pedja on 23.9.15. 17.12.
 * This class is part of the Tulfie
 * Copyright © 2015 ${OWNER}
 */
public class SRequestBuilder extends RequestBuilder
{
    public final boolean requiresAuthentication;

    public SRequestBuilder(Method method, boolean requiresAuthentication)
    {
        super(method);
        this.requiresAuthentication = requiresAuthentication;
    }
}
