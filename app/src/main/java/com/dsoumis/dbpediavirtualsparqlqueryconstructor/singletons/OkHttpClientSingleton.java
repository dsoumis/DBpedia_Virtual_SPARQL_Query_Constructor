package com.dsoumis.dbpediavirtualsparqlqueryconstructor.singletons;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class OkHttpClientSingleton {
    private static OkHttpClient okHttpClient = null;

    public static OkHttpClient getClient() {
        if (okHttpClient==null) {
            okHttpClient = new OkHttpClient().newBuilder()
                                             .retryOnConnectionFailure(true)
                                             .readTimeout(60, TimeUnit.SECONDS)
                                             .writeTimeout(60, TimeUnit.SECONDS)
                                             .callTimeout(60, TimeUnit.SECONDS).build();
        }
        return okHttpClient;
    }
}
