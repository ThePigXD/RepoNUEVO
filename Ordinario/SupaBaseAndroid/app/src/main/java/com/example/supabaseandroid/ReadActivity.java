package com.example.supabaseandroid;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Actividad para la consulta y visualización de productos en inventario.
 * @author Carlos M.
 */
public class ReadActivity extends AppCompatActivity {

    private final SupabaseRestClient mApiClient = new SupabaseRestClient();
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    private TextView mTvEstadoCarga;
    private LinearLayout mContainerLista;
    private LinearLayout mLayoutVacio;
    private MaterialButton mBtnRefrescar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_read);

        bindViews();
        setupListeners();
        setupInsets();

        cargarDatosInventario();
    }

    private void bindViews() {
        mTvEstadoCarga = findViewById(R.id.tv_estado_carga);
        mContainerLista = findViewById(R.id.container_items_lista);
        mLayoutVacio = findViewById(R.id.layout_vacio);
        mBtnRefrescar = findViewById(R.id.btn_refrescar_lista);
    }

    private void setupListeners() {
        mBtnRefrescar.setOnClickListener(v -> cargarDatosInventario());
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_read), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void cargarDatosInventario() {
        mTvEstadoCarga.setText(R.string.msg_cargando);
        mExecutor.execute(() -> {
            try {
                List<Producto> lista = mApiClient.getProductos();
                mMainHandler.post(() -> renderizarProductos(lista));
            } catch (Exception e) {
                mMainHandler.post(() -> {
                    String err = getString(R.string.err_conexion, e.getMessage());
                    mTvEstadoCarga.setText(err);
                    Toast.makeText(ReadActivity.this, err, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void renderizarProductos(List<Producto> productos) {
        mContainerLista.removeAllViews();

        if (productos == null || productos.isEmpty()) {
            mLayoutVacio.setVisibility(View.VISIBLE);
            mTvEstadoCarga.setText(R.string.msg_sin_registros);
            return;
        }

        mLayoutVacio.setVisibility(View.GONE);
        mTvEstadoCarga.setText(getString(R.string.msg_registros_encontrados, productos.size()));

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Producto item : productos) {
            View cardView = inflater.inflate(R.layout.item_producto, mContainerLista, false);

            TextView tvNombre = cardView.findViewById(R.id.tv_item_nombre);
            TextView tvPrecio = cardView.findViewById(R.id.tv_item_precio);
            TextView tvDescripcion = cardView.findViewById(R.id.tv_item_descripcion);

            tvNombre.setText(item.getProducto());
            tvPrecio.setText(String.format(Locale.getDefault(), "$%.2f", item.getPrecio()));
            tvDescripcion.setText(item.getDescripcion());

            mContainerLista.addView(cardView);
        }
    }

    @Override
    protected void onDestroy() {
        mExecutor.shutdown();
        super.onDestroy();
    }
}
