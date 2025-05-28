package com.example.hotelreservation.api;
import android.content.Context;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // Make sure this URL is correct and up-to-date with the server
    private static final String BASE_URL = "https://6915-62-174-205-48.ngrok-free.app";

    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            // Configurar interceptor para logging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY); // Esto logueará el cuerpo completo de requests y responses

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);
            
            // Añadir interceptor personalizado para mostrar información de red
            httpClient.addInterceptor(chain -> {
                android.util.Log.d("ApiClient", "Enviando solicitud a: " + chain.request().url());
                android.util.Log.d("ApiClient", "Headers: " + chain.request().headers());
                try {
                    okhttp3.Response response = chain.proceed(chain.request());
                    android.util.Log.d("ApiClient", "Recibida respuesta de: " + chain.request().url() + 
                                      " con código: " + response.code());
                    return response;
                } catch (Exception e) {
                    android.util.Log.e("ApiClient", "Error en solicitud a: " + chain.request().url(), e);
                    throw e;
                }
            });

            try {
                // Crear un TrustManager que confía en todos los certificados
                final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
                };

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}