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
import androidx.appcompat.app.AlertDialog;
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
 * Actividad para la baja y eliminación de productos de Supabase.
 * @author Carlos M.
 */
public class DeleteActivity extends AppCompatActivity {

    private final SupabaseRestClient mApiClient = new SupabaseRestClient();
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    private TextView mTvEstadoDelete;
    private LinearLayout mContainerDeleteItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delete);

        bindViews();
        setupInsets();

        cargarProductosParaEliminacion();
    }

    private void bindViews() {
        mTvEstadoDelete = findViewById(R.id.tv_estado_delete);
        mContainerDeleteItems = findViewById(R.id.container_delete_items);
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_delete), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void cargarProductosParaEliminacion() {
        mExecutor.execute(() -> {
            try {
                List<Producto> lista = mApiClient.getProductos();
                mMainHandler.post(() -> renderizarListaEliminable(lista));
            } catch (Exception e) {
                mMainHandler.post(() -> Toast.makeText(DeleteActivity.this,
                        getString(R.string.err_conexion, e.getMessage()), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void renderizarListaEliminable(List<Producto> productos) {
        mContainerDeleteItems.removeAllViews();

        if (productos == null || productos.isEmpty()) {
            mTvEstadoDelete.setText(R.string.msg_sin_registros);
            return;
        }

        mTvEstadoDelete.setText(R.string.msg_instruccion_delete);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Producto item : productos) {
            View cardView = inflater.inflate(R.layout.item_producto, mContainerDeleteItems, false);

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
            btnEditar.setVisibility(View.GONE);

            btnEliminar.setOnClickListener(v -> confirmarYEliminar(item));

            mContainerDeleteItems.addView(cardView);
        }
    }

    private void confirmarYEliminar(Producto item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_confirm_delete_title)
                .setMessage(getString(R.string.dialog_confirm_delete_msg, item.getProducto()))
                .setPositiveButton(R.string.btn_eliminar, (dialog, which) -> ejecutarBajaProducto(item.getId()))
                .setNegativeButton(R.string.btn_cancelar, null)
                .show();
    }

    private void ejecutarBajaProducto(String idProducto) {
        mExecutor.execute(() -> {
            try {
                mApiClient.eliminarProducto(idProducto);
                mMainHandler.post(() -> {
                    Toast.makeText(DeleteActivity.this, R.string.msg_eliminado_exito, Toast.LENGTH_SHORT).show();
                    cargarProductosParaEliminacion();
                });
            } catch (Exception ex) {
                mMainHandler.post(() -> Toast.makeText(DeleteActivity.this,
                        getString(R.string.err_conexion, ex.getMessage()), Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        mExecutor.shutdown();
        super.onDestroy();
    }
}
