package com.example.shopping.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.shopping.R
import com.example.shopping.ui.main.MainActivity
import com.example.shopping.ui.user.adapter.BannerAdapter
import com.example.shopping.ui.home.adapter.ProductAdapter
import com.example.shopping.ui.login.LoginActivity
import com.example.shopping.utils.FavoriteManager
import com.example.shopping.utils.UserSession
import com.google.firebase.firestore.FirebaseFirestore
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class HomeFragment : Fragment() {

    private lateinit var bannerViewPager: ViewPager2
    private lateinit var dotsIndicator: DotsIndicator
    private lateinit var searchAdapter: ProductAdapter
    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        rootView = view
        setupBanner(view)
        setupProductSections(view)
        setupSearch(view)


        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (UserSession.isRecommendForCoupon && UserSession.needMoreAmount > 0) {
            showCouponRecommend()
        }
    }



    private fun setupBanner(view: View) {
        bannerViewPager = view.findViewById(R.id.bannerViewPager)
        dotsIndicator = view.findViewById(R.id.dots_indicator)

        val images = listOf(
            R.drawable.nike,
            R.drawable.puma,
            R.drawable.adidas
        )

        val adapter = BannerAdapter(images)
        bannerViewPager.adapter = adapter
        dotsIndicator.attachTo(bannerViewPager)

        autoScroll()
    }

    private fun autoScroll() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val next = (bannerViewPager.currentItem + 1) % bannerViewPager.adapter!!.itemCount
                bannerViewPager.setCurrentItem(next, true)
                handler.postDelayed(this, 5000)
            }
        }
        handler.post(runnable)
    }

    private fun setupProductSections(view: View) {
        val recyclerNike = view.findViewById<RecyclerView>(R.id.recycler_hot_items)
        val recyclerPuma = view.findViewById<RecyclerView>(R.id.recycler_new_items)
        val recyclerAdidas = view.findViewById<RecyclerView>(R.id.recycler_recommend_items)

        setupRecycler(recyclerNike, ProductDataSource.allProducts.filter { it.id.toInt() in 1..6 })
        setupRecycler(recyclerPuma, ProductDataSource.allProducts.filter { it.id.toInt() in 7..12 })
        setupRecycler(recyclerAdidas, ProductDataSource.allProducts.filter { it.id.toInt() in 13..18 })
    }

    private fun setupSearch(view: View) {
        val recyclerSearch = view.findViewById<RecyclerView>(R.id.recycler_search_result)
        recyclerSearch.layoutManager = GridLayoutManager(requireContext(), 2)

        searchAdapter = ProductAdapter(
            items = emptyList(),
            onItemClick = { product ->
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeToProductDetail(product)
                )
            },
            onFavoriteClick = { product ->
                product.isFavorite = !product.isFavorite
                searchAdapter.notifyDataSetChanged()
            },
            onRequireLogin = {
                Toast.makeText(requireContext(), "請先登入", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }
        )

        recyclerSearch.adapter = searchAdapter

        setupSearchListener(recyclerSearch)
    }
    private fun setupSearchListener(recyclerSearch: RecyclerView) {
        val searchInput = (requireActivity() as MainActivity).getSearchInput()

        searchInput.addTextChangedListener { text ->
            val keyword = text.toString().trim()

            if (keyword.isEmpty()) {
                resetToHomeState()
                return@addTextChangedListener
            }

            val result = ProductDataSource.allProducts.filter {
                it.name.contains(keyword, true)
            }

            searchAdapter.updateList(result)
            recyclerSearch.visibility = View.VISIBLE

            showHomeSections( false)
            view?.findViewById<View>(R.id.bannerViewPager)?.visibility = View.GONE
            view?.findViewById<View>(R.id.dots_indicator)?.visibility = View.GONE
        }
    }


    private fun showHomeSections(show: Boolean) {
        val visibility = if (show) View.VISIBLE else View.GONE

        rootView.findViewById<View>(R.id.bannerViewPager)?.visibility = visibility
        rootView.findViewById<View>(R.id.dots_indicator)?.visibility = visibility

        rootView.findViewById<View>(R.id.section_title_1)?.visibility = visibility
        rootView.findViewById<View>(R.id.section_title_2)?.visibility = visibility
        rootView.findViewById<View>(R.id.section_title_3)?.visibility = visibility

        rootView.findViewById<View>(R.id.recycler_hot_items)?.visibility = visibility
        rootView.findViewById<View>(R.id.recycler_new_items)?.visibility = visibility
        rootView.findViewById<View>(R.id.recycler_recommend_items)?.visibility = visibility


    }

    private fun setupRecycler(recycler: RecyclerView, items: List<Product>) {
        recycler.layoutManager = GridLayoutManager(requireContext(),2)

        recycler.adapter = ProductAdapter(
            items = items,

            onItemClick = { product ->
                val action = HomeFragmentDirections.actionHomeToProductDetail(product)
                findNavController().navigate(action)
            },

            onFavoriteClick = { product ->
                val newState = FavoriteManager.toggleFavorite(
                    requireContext(),
                    product.id
                )
                product.isFavorite = newState
                recycler.adapter?.notifyDataSetChanged()
            },
            onRequireLogin = {
                Toast.makeText(requireContext(),"請先登入",Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }
        )
    }
    private fun resetToHomeState(){
        view?.findViewById<RecyclerView>(R.id.recycler_search_result)?.visibility = View.GONE

        view?.findViewById<View>(R.id.bannerViewPager)?.visibility = View.VISIBLE
        view?.findViewById<View>(R.id.dots_indicator)?.visibility = View.VISIBLE

        showHomeSections(true)
    }
    private fun showCouponRecommend() {
        val needMore = UserSession.needMoreAmount
        if (needMore <= 0) return
        showHomeSections(false)

        rootView.findViewById<View>(R.id.bannerViewPager)?.visibility = View.GONE
        rootView.findViewById<View>(R.id.dots_indicator)?.visibility = View.GONE
        rootView.findViewById<View>(R.id.section_title_1)?.visibility = View.GONE
        rootView.findViewById<View>(R.id.section_title_2)?.visibility = View.GONE
        rootView.findViewById<View>(R.id.section_title_3)?.visibility = View.GONE

        val recyclerSearch = rootView.findViewById<RecyclerView>(R.id.recycler_search_result)
        recyclerSearch.visibility = View.VISIBLE
        recyclerSearch.isClickable = true
        recyclerSearch.isFocusable = true

        (requireActivity() as MainActivity).clearSearchFocus()

        val recommendList = ProductDataSource.allProducts
            .filter { it.price >= needMore }
            .sortedBy { it.price - needMore }
            .take(3)

        if (recommendList.isEmpty()) return
        searchAdapter = ProductAdapter(
            items = emptyList(),
            onItemClick = { product -> findNavController().navigate(HomeFragmentDirections.actionHomeToProductDetail(product))},
            onFavoriteClick = { product ->
                product.isFavorite = !product.isFavorite
                searchAdapter.notifyDataSetChanged()
            },
            onRequireLogin = {
                Toast.makeText(requireContext(), "請先登入", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            },
            onQuickAddCart = { product ->
                quickAddToCart(product)
            }
        )
        recyclerSearch.adapter = searchAdapter
        searchAdapter.updateList(recommendList)
        Toast.makeText(
            requireContext(),
            "🔥 再買 NT$$needMore 即可使用優惠",
            Toast.LENGTH_SHORT
        ).show()
        UserSession.isRecommendForCoupon = true
    }
    private fun quickAddToCart(product: Product) {
        if (!UserSession.isLogin) {
            Toast.makeText(requireContext(), "請先登入", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            return
        }

        val db = FirebaseFirestore.getInstance()

        val cartRef = db.collection("users")
            .document(UserSession.documentId)
            .collection("cart")

        val cartItem = hashMapOf(
            "productId" to product.id,
            "productName" to product.name,
            "price" to product.price,
            "size" to "預設",
            "quantity" to 1,
            "imageResId" to product.imageResId[0]
        )

        cartRef.add(cartItem).addOnSuccessListener { docRef ->
            UserSession.pendingSelectCartId = docRef.id
            Toast.makeText(
                requireContext(),
                "已加入購物車，優惠重新計算中",
                Toast.LENGTH_SHORT
            ).show()
            UserSession.isRecommendForCoupon = false
            UserSession.needMoreAmount = 0
            findNavController().navigate(R.id.navigation_cart)
        }
    }


    override fun onResume() {
        super.onResume()
        val main = requireActivity() as MainActivity
        main.showHomeToolbar()
        main.hideProductActionBar()
        main.setFavoriteMenuVisible(true)
        main.clearSearch()
        if (UserSession.isRecommendForCoupon && UserSession.needMoreAmount > 0) {
            showCouponRecommend()
        } else {
            resetToHomeState()
        }
    }
}
