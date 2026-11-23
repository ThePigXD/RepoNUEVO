package com.example.supabaseandroid;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Modelo de datos para la entidad Producto almacenada en Supabase.
 * Dev: Carlos M.
 */
public class Producto {
    private String id;
    private String producto;
    private String descripcion;
    private double precio;

    public Producto() {
    }

    public Producto(String id, String producto, String descripcion, double precio) {
        this.id = id;
        this.producto = producto;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    /**
     * Convierte un objeto JSONObject proveniente de Supabase a una instancia de Producto.
     */
    public static Producto fromJson(JSONObject jsonObject) throws JSONException {
        Producto p = new Producto();
        if (jsonObject.has(SupabaseConfig.ID_COLUMN)) {
            p.setId(jsonObject.getString(SupabaseConfig.ID_COLUMN));
        }
        p.setProducto(jsonObject.optString("producto", ""));
        p.setDescripcion(jsonObject.optString("descripcion", ""));
        p.setPrecio(jsonObject.optDouble("precio", 0.0));
        return p;
    }

    /**
     * Serializa el objeto Producto a JSONObject para peticiones POST/PATCH.
     */
    public JSONObject toJson() throws JSONException {
        JSONObject payload = new JSONObject();
        payload.put("producto", this.producto);
        payload.put("descripcion", this.descripcion);
        payload.put("precio", this.precio);
        return payload;
    }

    @Override
    public String toString() {
        return producto + " ($" + String.format("%.2f", precio) + ")";
    }
}
