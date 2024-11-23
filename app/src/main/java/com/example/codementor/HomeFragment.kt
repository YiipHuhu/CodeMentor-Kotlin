package com.example.codementor

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.codementor.activities.ExplanationActivity
import com.example.codementor.activities.ModuloAdapter
import com.example.codementor.activities.Modulos

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var moduloAdapter: ModuloAdapter
    private lateinit var moduloList: List<Modulos>
    private lateinit var filteredModuloList: MutableList<Modulos>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)  // Habilitar menu para este fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        moduloList = listOf(
            Modulos("Introdução a Kotlin", "Kotlin", "13m"),
            Modulos("Declaração de variáveis em Kotlin", "Kotlin", "10m"),
            Modulos("O que é RecyclerView", "Kotlin", "20m"),
            Modulos("Tudo sobre Kotlin", "Kotlin", "30m"),
            Modulos("Elementos comuns de um layout XML", "XML/Kotlin", "15m"),
            Modulos("Layout e posicionamento de componentes", "XML/Kotlin", "30m"),
            Modulos("Estilos e temas", "Kotlin", "25m"),
            Modulos("Estrutura de um projeto Android em Kotlin", "Kotlin", "40m")
        )

        filteredModuloList = moduloList.toMutableList()

        // Configurar Adapter com callback
        moduloAdapter = ModuloAdapter(filteredModuloList) { modulo ->
            openModuloDetail(modulo)
        }
        recyclerView.adapter = moduloAdapter
    }

    private fun openModuloDetail(modulo: Modulos) {
        val intent = Intent(requireContext(), ExplanationActivity::class.java)
        intent.putExtra("moduloName", modulo.name) // Passa dados para a Activity
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_home, menu)  // Infla o menu com SearchView

        // Encontrar o SearchView e configurar o listener para captura de texto
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.queryHint = "Buscar por módulo..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Não fazemos nada aqui, já que a pesquisa é realizada enquanto o texto é alterado
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filtra a lista enquanto o texto é alterado
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(query: String?) {
        filteredModuloList.clear()
        if (query.isNullOrEmpty()) {
            filteredModuloList.addAll(moduloList)
        } else {
            for (modulo in moduloList) {
                if (modulo.name.contains(query, ignoreCase = true)) {
                    filteredModuloList.add(modulo)
                }
            }
        }
        moduloAdapter.notifyDataSetChanged()  // Notifica o adapter para atualizar a lista
    }
}
