package com.example.fragmentado;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InicioFragment extends Fragment {

    private int contadorClics = 0;

    public InicioFragment() {
        super(R.layout.fragment_inicio);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvStatus = view.findViewById(R.id.tv_status_inicio);
        Button btnAction = view.findViewById(R.id.btn_action_inicio);

        btnAction.setOnClickListener(v -> {
            contadorClics++;
            tvStatus.setText(getString(R.string.status_inicio_updated, contadorClics));
        });
    }
}
