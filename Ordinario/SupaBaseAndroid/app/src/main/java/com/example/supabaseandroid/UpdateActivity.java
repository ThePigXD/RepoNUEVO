package com.example.supabaseandroid;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Actividad para la actualización de productos en Supabase.
 * @author Carlos M.
 */
public class UpdateActivity extends AppCompatActivity {

    private final SupabaseRestClient mApiClient = new SupabaseRestClient();
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    private TextInputLayout mTilNombre;
    private TextInputLayout mTilDescripcion;
    private TextInputLayout mTilPrecio;
    private TextInputEditText mEtNombre;
    private TextInputEditText mEtDescripcion;
    private TextInputEditText mEtPrecio;
    private MaterialButton mBtnGuardar;
    private MaterialButton mBtnCancelar;
    private TextView mTvEstadoEdicion;
    private LinearLayout mContainerItems;

    private String mProductoSeleccionadoId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update);

        bindViews();
        setupListeners();
        setupInsets();

        cargarProductosParaEdicion();
    }

    private void bindViews() {
        mTilNombre = findViewById(R.id.til_edit_nombre);
        mTilDescripcion = findViewById(R.id.til_edit_descripcion);
        mTilPrecio = findViewById(R.id.til_edit_precio);

        mEtNombre = findViewById(R.id.et_edit_nombre);
        mEtDescripcion = findViewById(R.id.et_edit_descripcion);
        mEtPrecio = findViewById(R.id.et_edit_precio);

        mBtnGuardar = findViewById(R.id.btn_guardar_edicion);
        mBtnCancelar = findViewById(R.id.btn_cancelar_edicion);
        mTvEstadoEdicion = findViewById(R.id.tv_estado_edicion);
        mContainerItems = findViewById(R.id.container_update_items);
    }

    private void setupListeners() {
        mBtnGuardar.setOnClickListener(v -> guardarCambiosProducto());
        mBtnCancelar.setOnClickListener(v -> cancelarEdicion());
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_update), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void cargarProductosParaEdicion() {
        mExecutor.execute(() -> {
            try {
                List<Producto> lista = mApiClient.getProductos();
                mMainHandler.post(() -> renderizarListaEditable(lista));
            } catch (Exception e) {
                mMainHandler.post(() -> Toast.makeText(UpdateActivity.this,
                        getString(R.string.err_conexion, e.getMessage()), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void renderizarListaEditable(List<Producto> productos) {
        mContainerItems.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Producto item : productos) {
            View cardView = inflater.inflate(R.layout.item_producto, mContainerItems, false);

            TextView tvNombre = cardView.findViewById(R.id.tv_item_nombre);
            TextView tvPrecio = cardView.findViewById(R.id.tv_item_precio);
            TextView tvDescripcion = cardView.findViewById(R.id.tv_item_descripcion);
            LinearLayout layoutAcciones = cardView.findViewById(R.id.layout_acciones);
            MaterialButton btnEditar = cardView.findViewById(R.id.btn_item_editar);
            MaterialButton btnEliminar = cardView.findViewById(R.id.btn_item_eliminar);

            tvNombre.setText(item.getProducto());
            tvPrecio.setText(String.format(Locale.getDefault(), "$%.2f", item.getPrecio()));
            tvDescripcion.setText(item.getDescripcion());

            layoutAcciones.setVisibility(View.VISIBLE);
            btnEliminar.setVisibility(View.GONE);

            btnEditar.setOnClickListener(v -> seleccionarProductoParaEdicion(item));
            cardView.setOnClickListener(v -> seleccionarProductoParaEdicion(item));

            mContainerItems.addView(cardView);
        }
    }

    private void seleccionarProductoParaEdicion(Producto item) {
        mProductoSeleccionadoId = item.getId();
        mEtNombre.setText(item.getProducto());
        mEtDescripcion.setText(item.getDescripcion());
        mEtPrecio.setText(String.format(Locale.getDefault(), "%.2f", item.getPrecio()));

        mBtnGuardar.setEnabled(true);
        mBtnCancelar.setVisibility(View.VISIBLE);

        mTvEstadoEdicion.setText(getString(R.string.editando_producto, item.getProducto()));
        mTilNombre.requestFocus();
    }

    private void guardarCambiosProducto() {
        if (mProductoSeleccionadoId == null) {
            Toast.makeText(this, R.string.err_sin_seleccion, Toast.LENGTH_SHORT).show();
            return;
        }

        limpiarErrores();
        String nombre = mEtNombre.getText() != null ? mEtNombre.getText().toString().trim() : "";
        String descripcion = mEtDescripcion.getText() != null ? mEtDescripcion.getText().toString().trim() : "";
        String precioVal = mEtPrecio.getText() != null ? mEtPrecio.getText().toString().trim().replace(',', '.') : "";

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(descripcion) || TextUtils.isEmpty(precioVal)) {
            Toast.makeText(this, R.string.err_campos_vacios, Toast.LENGTH_SHORT).show();
            return;
        }

        final double precio;
        try {
            precio = Double.parseDouble(precioVal);
        } catch (NumberFormatException e) {
            mTilPrecio.setError(getString(R.string.err_precio_invalido));
            return;
        }

        Producto productoActualizado = new Producto(mProductoSeleccionadoId, nombre, descripcion, precio);
        mBtnGuardar.setEnabled(false);

        mExecutor.execute(() -> {
            try {
                mApiClient.actualizarProducto(productoActualizado);
                mMainHandler.post(() -> {
                    Toast.makeText(UpdateActivity.this, R.string.msg_actualizado_exito, Toast.LENGTH_SHORT).show();
                    cancelarEdicion();
                    cargarProductosParaEdicion();
                });
            } catch (Exception ex) {
                mMainHandler.post(() -> {
                    mBtnGuardar.setEnabled(true);
                    Toast.makeText(UpdateActivity.this, getString(R.string.err_conexion, ex.getMessage()), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void cancelarEdicion() {
        mProductoSeleccionadoId = null;
        mEtNombre.setText("");
        mEtDescripcion.setText("");
        mEtPrecio.setText("");
        limpiarErrores();

        mBtnGuardar.setEnabled(false);
        mBtnCancelar.setVisibility(View.GONE);
        mTvEstadoEdicion.setText(R.string.msg_instruccion_update);
    }

    private void limpiarErrores() {
        mTilNombre.setError(null);
        mTilDescripcion.setError(null);
        mTilPrecio.setError(null);
    }

    @Override
    protected void onDestroy() {
        mExecutor.shutdown();
        super.onDestroy();
    }
}
