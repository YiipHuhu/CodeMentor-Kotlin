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
            Modulos("Declaração de variáveis em Kotlin", "Kotlin", "10m"),
            Modulos("O que é RecyclerView", "Kotlin", "20m"),
            Modulos("Tudo sobre Kotlin", "Kotlin", "30m"),
            Modulos("Elementos comuns de um layout XML", "XML/Kotlin", "15m"),
            Modulos("Layout e posicionamento de componentes", "XML/Kotlin", "30m"),
            Modulos("Estilos e temas", "Kotlin", "25m"),
            Modulos("Estrutura de um projeto Android em Kotlin", "Kotlin", "40m")
        )

        // Configurar Adapter
        moduloAdapter = ModuloAdapter(moduloList)
        recyclerView.adapter = moduloAdapter
    }
}
