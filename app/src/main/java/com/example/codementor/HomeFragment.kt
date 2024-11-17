package com.example.codementor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var moduloAdapter: ModuloAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate o layout do fragmento
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Lista de módulos
        val moduloList = listOf(
            Modulos("Introdução a Kotlin", "Kotlin", "13m"),
            Modulos("Definindo variaveis", "Kotlin", "10m"),
            Modulos("Teste", "xml/Kotlin", "60m"),
            Modulos("Teste", "xml/Kotlin", "60m"),
            Modulos("Teste", "xml/Kotlin", "60m"),
            Modulos("Teste", "xml/Kotlin", "60m"),
            Modulos("Teste", "xml/Kotlin", "60m"),
            Modulos("Teste", "xml/Kotlin", "60m"),
            Modulos("Teste", "xml/Kotlin", "60m"),
            Modulos("Teste", "xml/Kotlin", "60m"),
            Modulos("Teste", "xml/Kotlin", "60m")
        )

        // Configurar Adapter
        moduloAdapter = ModuloAdapter(moduloList)
        recyclerView.adapter = moduloAdapter
    }
}
