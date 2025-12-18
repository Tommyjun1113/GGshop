package com.example.shopping.ui.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping.R
import com.example.shopping.ui.home.ProductDataSource
import com.example.shopping.ui.home.adapter.ProductAdapter
import com.example.shopping.ui.main.MainActivity
import com.example.shopping.utils.FavoriteManager

class FavorFragment : Fragment() {
    private lateinit var recyclerFavor : RecyclerView
    private lateinit var layoutEmpty : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_favor, container, false)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = "我的最愛"
            setDisplayHomeAsUpEnabled(true)
        }
        recyclerFavor = view.findViewById(R.id.recyclerFavor)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)
        recyclerFavor.layoutManager = GridLayoutManager(requireContext(), 2)
        loadFavoriteProducts()
    }
    private fun loadFavoriteProducts() {
        val favoriteIds = FavoriteManager.getFavorites(requireContext())

        val favoriteProducts = ProductDataSource.allProducts
            .filter { it.id in favoriteIds }

        recyclerFavor.adapter = ProductAdapter(
            favoriteProducts,
            onItemClick = { product ->
                val action = FavorFragmentDirections.actionFavorToProductDetail(product)
                findNavController().navigate(action)
            },
            onFavoriteClick = { product ->
                val newState = FavoriteManager.toggleFavorite(
                    requireContext(),
                    product.id
                )
                product.isFavorite = newState
                loadFavoriteProducts()
            },
            onRequireLogin = {

            }
        )
        layoutEmpty.visibility =
            if (favoriteProducts.isEmpty()) View.VISIBLE else View.GONE
    }




    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            findNavController().navigateUp()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onResume() {
        super.onResume()
        val main = requireActivity() as MainActivity
        main.showProductToolbar("我的最愛")
        main.hideProductActionBar()
        main.setFavoriteMenuVisible(false)

        loadFavoriteProducts()
    }
    override fun onStop() {
        super.onStop()
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.VISIBLE
    }
    override fun onDestroyView() {
        super.onDestroyView()
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
}
