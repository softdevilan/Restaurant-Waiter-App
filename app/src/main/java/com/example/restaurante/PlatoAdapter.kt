package com.example.restaurante

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlatoAdapter(
    private val context: Context,
    private val platos: List<Plato>,
    private val onAddClick: (Plato) -> Unit
) : RecyclerView.Adapter<PlatoAdapter.PlatoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_plato, parent, false)
        return PlatoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlatoViewHolder, position: Int) {
        holder.bind(platos[position])
    }

    override fun getItemCount(): Int = platos.size

    inner class PlatoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgPlato: ImageView = itemView.findViewById(R.id.imgPlato)
        private val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        private val txtPrecio: TextView = itemView.findViewById(R.id.txtPrecio)
        private val btnAgregar: Button = itemView.findViewById(R.id.btnAgregar)

        fun bind(plato: Plato) {
            txtNombre.text = plato.nombre
            txtPrecio.text = "€${plato.precio}"
            val resID = context.resources.getIdentifier(plato.foto, "drawable", context.packageName)
            imgPlato.setImageResource(resID)

            btnAgregar.setOnClickListener {
                onAddClick(plato) // Llama a la función cuando se presiona el botón
            }
        }
    }
}

