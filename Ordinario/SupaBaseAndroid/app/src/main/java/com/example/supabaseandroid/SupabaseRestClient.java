package com.example.supabaseandroid;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Cliente REST encargado de la comunicación HTTP con la API de Supabase.
 * Dev: Carlos M.
 */
public final class SupabaseRestClient {

    private static final String LOG_TAG = "SupabaseRestClient";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient mClient;

    public SupabaseRestClient() {
        this.mClient = new OkHttpClient.Builder()
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Consulta el catálogo completo de productos ordenados por ID desc.
     */
    public List<Producto> getProductos() throws IOException, JSONException {
        String urlQuery = "?select=*&order=" + SupabaseConfig.ID_COLUMN + ".desc";
        String responseBody = executeHttpRequest("GET", buildEndpointUrl(urlQuery), null);

        JSONArray jsonArray = new JSONArray(responseBody);
        List<Producto> listaProductos = new ArrayList<>();
        int count = jsonArray.length();

        for (int i = 0; i < count; i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            listaProductos.add(Producto.fromJson(obj));
        }

        Log.d(LOG_TAG, "Se obtuvieron " + listaProductos.size() + " productos de Supabase.");
        return listaProductos;
    }

    /**
     * Inserta un nuevo producto en la tabla de Supabase.
     */
    public void crearProducto(Producto item) throws IOException, JSONException {
        executeHttpRequest("POST", buildEndpointUrl(""), item.toJson());
        Log.d(LOG_TAG, "Producto insertado correctamente.");
    }

    /**
     * Actualiza los campos de un producto existente por su ID.
     */
    public void actualizarProducto(Producto item) throws IOException, JSONException {
        String filterParam = "?" + SupabaseConfig.ID_COLUMN + "=eq." + Uri.encode(item.getId());
        executeHttpRequest("PATCH", buildEndpointUrl(filterParam), item.toJson());
        Log.d(LOG_TAG, "Producto ID " + item.getId() + " actualizado correctamente.");
    }

    /**
     * Elimina un producto según su identificador único.
     */
    public void eliminarProducto(String idProducto) throws IOException {
        String filterParam = "?" + SupabaseConfig.ID_COLUMN + "=eq." + Uri.encode(idProducto);
        executeHttpRequest("DELETE", buildEndpointUrl(filterParam), null);
        Log.d(LOG_TAG, "Producto ID " + idProducto + " eliminado correctamente.");
    }

    // Helper privado para ejecutar peticiones HTTP sincrónicas
    private String executeHttpRequest(String httpMethod, String targetUrl, JSONObject payload) throws IOException {
        String apiKey = SupabaseConfig.ANON_KEY.trim();

        Request.Builder requestBuilder = new Request.Builder()
                .url(targetUrl)
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Accept", "application/json")
                .addHeader("Prefer", "return=representation");

        if (payload != null) {
            requestBuilder.addHeader("Content-Type", "application/json")
                    .method(httpMethod, RequestBody.create(payload.toString(), JSON_MEDIA_TYPE));
        } else {
            requestBuilder.method(httpMethod, null);
        }

        Request request = requestBuilder.build();

        try (Response response = mClient.newCall(request).execute()) {
            String responseStr = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                Log.e(LOG_TAG, "Error HTTP " + response.code() + ": " + responseStr);
                throw new IOException("Supabase devolvió código " + response.code()
                        + (responseStr.isEmpty() ? "" : " - " + responseStr));
            }
            return responseStr;
        }
    }

    private String buildEndpointUrl(String queryParams) {
        String baseUrl = SupabaseConfig.URL.trim().replaceAll("/+$", "");
        return baseUrl + "/rest/v1/" + SupabaseConfig.TABLE_PRODUCTOS + queryParams;
    }
}
