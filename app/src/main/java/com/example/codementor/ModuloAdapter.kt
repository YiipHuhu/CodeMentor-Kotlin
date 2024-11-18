package com.example.codementor

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ModuloAdapter(
    private val moduloList: List<Modulos>,
    private val onItemClick: (Modulos) -> Unit // Callback para cliques
) : RecyclerView.Adapter<ModuloAdapter.ModuloViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuloViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_modulos, parent, false)
        return ModuloViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuloViewHolder, position: Int) {
        val modulo = moduloList[position]
        holder.bind(modulo)

        holder.itemView.setOnClickListener {
            onItemClick(modulo)
        }
    }

    override fun getItemCount(): Int = moduloList.size

    class ModuloViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        private val genreTextView: TextView = itemView.findViewById(R.id.textViewtipo)
        private val timeTextView: TextView = itemView.findViewById(R.id.textViewTempo)

        fun bind(modulo: Modulos) {
            nameTextView.text = modulo.name
            genreTextView.text = modulo.genre
            timeTextView.text = modulo.time
        }
    }
}