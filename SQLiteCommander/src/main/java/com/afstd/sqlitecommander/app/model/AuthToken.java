package com.afstd.sqlitecommander.app.model;

/**
 * Created by pedja on 11.2.16..
 */
public class AuthToken
{
    public String accessToken, refreshToken, tokenType, scope, accountName;
    public long expiresIn;
}
