package com.example.listas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class VideojuegoAdapter extends RecyclerView.Adapter<VideojuegoAdapter.VideojuegoViewHolder> {

    private final ArrayList<Videojuego> listaVideojuegos;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Videojuego videojuego);
    }

    public VideojuegoAdapter(ArrayList<Videojuego> listaVideojuegos) {
        this.listaVideojuegos = listaVideojuegos;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideojuegoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_videojuego, parent, false);
        return new VideojuegoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideojuegoViewHolder holder, int position) {
        Videojuego videojuegoActual = listaVideojuegos.get(position);

        holder.textTitulo.setText(videojuegoActual.getTitulo());
        holder.textGenero.setText(videojuegoActual.getGenero());
        holder.textPlataforma.setText(String.format(Locale.getDefault(), "%s • %d", videojuegoActual.getPlataforma(), videojuegoActual.getAnioLanzamiento()));
        holder.textCalificacion.setText(String.format(Locale.getDefault(), "★ %.1f", videojuegoActual.getCalificacion()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(videojuegoActual);
            } else {
                Toast.makeText(v.getContext(),
                        "Seleccionaste: " + videojuegoActual.getTitulo() + " (" + videojuegoActual.getPlataforma() + ")",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaVideojuegos != null ? listaVideojuegos.size() : 0;
    }

    public static class VideojuegoViewHolder extends RecyclerView.ViewHolder {
        TextView textTitulo;
        TextView textGenero;
        TextView textPlataforma;
        TextView textCalificacion;

        public VideojuegoViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.text_titulo_videojuego);
            textGenero = itemView.findViewById(R.id.text_genero_videojuego);
            textPlataforma = itemView.findViewById(R.id.text_plataforma_videojuego);
            textCalificacion = itemView.findViewById(R.id.text_calificacion_videojuego);
        }
    }
}
