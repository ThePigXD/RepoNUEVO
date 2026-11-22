package com.example.fragmentado;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class NovedadesFragment extends Fragment {

    private int publicaciones = 5;

    public NovedadesFragment() {
        super(R.layout.fragment_novedades);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvStatus = view.findViewById(R.id.tv_status_novedades);
        Button btnAction = view.findViewById(R.id.btn_action_novedades);

        btnAction.setOnClickListener(v -> {
            publicaciones += 3;
            tvStatus.setText(getString(R.string.status_novedades_updated, publicaciones));
        });
    }
}
