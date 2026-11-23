package com.example.supabaseandroid;

/**
 * Constantes de configuración y credenciales para el servicio de Supabase.
 * @author Carlos M. - Dev Team
 */
public final class SupabaseConfig {

    // Credenciales por defecto
    public static final String DEFAULT_URL = "https://myvxnpymhbhuncnqdkhz.supabase.co/";
    public static final String DEFAULT_ANON_KEY = "sb_publishable_iEcSFA7bxJBHkfkbmoeGBw_0huFkxZH";

    // Credenciales leídas de BuildConfig
    public static final String URL = BuildConfig.SUPABASE_URL;
    public static final String ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;

    // Nombre de la tabla e identificador primario en Supabase
    public static final String TABLE_PRODUCTOS = "productos";
    public static final String ID_COLUMN = "id";

    private SupabaseConfig() {
    }
}
