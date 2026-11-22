package com.example.fragmentado;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PerfilFragment extends Fragment {

    private boolean estadoActivo = true;

    public PerfilFragment() {
        super(R.layout.fragment_perfil);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvStatus = view.findViewById(R.id.tv_status_perfil);
        Button btnAction = view.findViewById(R.id.btn_action_perfil);

        btnAction.setOnClickListener(v -> {
            estadoActivo = !estadoActivo;
            tvStatus.setText(getString(estadoActivo ? R.string.status_perfil_online : R.string.status_perfil_busy));
        });
    }
}
