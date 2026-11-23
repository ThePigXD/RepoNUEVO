package com.example.supabaseandroid;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Actividad para el alta de nuevos productos en el catálogo de Supabase.
 * @author Carlos M.
 */
public class CreateActivity extends AppCompatActivity {

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
    private MaterialButton mBtnLimpiar;
    private MaterialCardView mCardEstado;
    private TextView mTvEstado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create);

        bindViews();
        setupListeners();
        setupInsets();
    }

    private void bindViews() {
        mTilNombre = findViewById(R.id.til_nombre);
        mTilDescripcion = findViewById(R.id.til_descripcion);
        mTilPrecio = findViewById(R.id.til_precio);

        mEtNombre = findViewById(R.id.et_nombre_prod);
        mEtDescripcion = findViewById(R.id.et_descripcion_prod);
        mEtPrecio = findViewById(R.id.et_precio_prod);

        mBtnGuardar = findViewById(R.id.btn_grabar_datos);
        mBtnLimpiar = findViewById(R.id.btn_limpiar_formulario);

        mCardEstado = findViewById(R.id.card_estado);
        mTvEstado = findViewById(R.id.tv_estado_operacion);
    }

    private void setupListeners() {
        mBtnGuardar.setOnClickListener(v -> procesarRegistroProducto());
        mBtnLimpiar.setOnClickListener(v -> resetFormulario());
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_create), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void procesarRegistroProducto() {
        limpiarErrores();

        String nombre = mEtNombre.getText() != null ? mEtNombre.getText().toString().trim() : "";
        String descripcion = mEtDescripcion.getText() != null ? mEtDescripcion.getText().toString().trim() : "";
        String precioVal = mEtPrecio.getText() != null ? mEtPrecio.getText().toString().trim().replace(',', '.') : "";

        boolean esValido = true;
        if (TextUtils.isEmpty(nombre)) {
            mTilNombre.setError(getString(R.string.err_campos_vacios));
            esValido = false;
        }
        if (TextUtils.isEmpty(descripcion)) {
            mTilDescripcion.setError(getString(R.string.err_campos_vacios));
            esValido = false;
        }
        if (TextUtils.isEmpty(precioVal)) {
            mTilPrecio.setError(getString(R.string.err_campos_vacios));
            esValido = false;
        }

        if (!esValido) {
            mostrarMensajeStatus(getString(R.string.err_campos_vacios), false);
            return;
        }

        final double precio;
        try {
            precio = Double.parseDouble(precioVal);
            if (precio < 0 || !Double.isFinite(precio)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            mTilPrecio.setError(getString(R.string.err_precio_invalido));
            mostrarMensajeStatus(getString(R.string.err_precio_invalido), false);
            return;
        }

        Producto nuevoProducto = new Producto(null, nombre, descripcion, precio);

        mostrarMensajeStatus(getString(R.string.msg_guardando), true);
        mBtnGuardar.setEnabled(false);

        mExecutor.execute(() -> {
            try {
                mApiClient.crearProducto(nuevoProducto);
                mMainHandler.post(() -> {
                    mBtnGuardar.setEnabled(true);
                    mostrarMensajeStatus(getString(R.string.msg_guardado_exito), true);
                    Toast.makeText(CreateActivity.this, R.string.msg_guardado_exito, Toast.LENGTH_SHORT).show();
                    resetFormulario();
                });
            } catch (Exception ex) {
                mMainHandler.post(() -> {
                    mBtnGuardar.setEnabled(true);
                    mostrarMensajeStatus(getString(R.string.err_conexion, ex.getMessage()), false);
                });
            }
        });
    }

    private void resetFormulario() {
        mEtNombre.setText("");
        mEtDescripcion.setText("");
        mEtPrecio.setText("");
        limpiarErrores();
        mEtNombre.requestFocus();
    }

    private void limpiarErrores() {
        mTilNombre.setError(null);
        mTilDescripcion.setError(null);
        mTilPrecio.setError(null);
    }

    private void mostrarMensajeStatus(String mensaje, boolean exito) {
        mCardEstado.setVisibility(View.VISIBLE);
        mTvEstado.setText(mensaje);
        int colorRes = exito ? R.color.status_success : R.color.status_error;
        mTvEstado.setTextColor(ContextCompat.getColor(this, colorRes));
    }

    @Override
    protected void onDestroy() {
        mExecutor.shutdown();
        super.onDestroy();
    }
}
