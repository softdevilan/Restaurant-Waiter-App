package com.example.restaurante

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PedidoAdapter(private val pedidoList: List<Pair<String, Pair<Int, Double>>>) :
    RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder>() {

    class PedidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPedido: TextView = itemView.findViewById(R.id.tvPedidoItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pedido, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val (nombre, pair) = pedidoList[position]
        holder.tvPedido.text = "${pair.first}x $nombre ${String.format("%.2f", pair.first * pair.second)}"
    }

    override fun getItemCount(): Int = pedidoList.size
}
