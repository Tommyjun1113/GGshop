package com.example.shopping.ui.cart

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopping.databinding.FragmentCartBinding
import com.example.shopping.ui.cart.adapter.CartAdapter
import com.example.shopping.ui.home.ProductDataSource
import com.example.shopping.ui.login.LoginActivity
import com.example.shopping.ui.main.MainActivity
import com.example.shopping.ui.user.order.OrderItem
import com.example.shopping.ui.user.order.OrderModel
import com.example.shopping.utils.UserSession
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.serialization.builtins.UIntArraySerializer

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CartViewModel by viewModels()
    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        binding.btnCheckout.setOnClickListener { checkout() }
        binding.btnDeleteSelected.setOnClickListener {
            viewModel.deleteSelectedItems()
        }
        binding.btnGoShopping.setOnClickListener {
            if (!UserSession.isLogin) {
                Toast.makeText(requireContext(), "請先登入!!!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            } else {
                (requireActivity() as MainActivity).navigateToHome()
            }
        }

        binding.checkAll.setOnCheckedChangeListener { _, checked ->
            viewModel.toggleSelectAll(checked)
        }

        viewModel.loadCart()
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter(
            emptyList(),
            object : CartAdapter.CartActions {
                override fun onSelectionChanged(item: CartItem, checked: Boolean) {
                    viewModel.updateSelection(item, checked)
                }

                override fun increase(item: CartItem) =
                    viewModel.updateQty(item, item.quantity + 1)

                override fun decrease(item: CartItem) {
                    if (item.quantity > 1)
                        viewModel.updateQty(item, item.quantity - 1)
                }

                override fun delete(item: CartItem) =
                    viewModel.deleteItem(item)

                override fun onItemClick(item: CartItem) =
                    openProduct(item.productId)
            }
        )

        binding.recyclerCart.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCart.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.cartItems.observe(viewLifecycleOwner) { list ->
            adapter.updateList(list)
            updateCartUI(list)
        }
        viewModel.discount.observe(viewLifecycleOwner) { discount ->
            if(discount > 0){
                binding.txtDiscount.visibility = View.VISIBLE
                binding.txtDiscount.text = "已套用優惠：-NT$$discount"
            } else {
                binding.txtDiscount.visibility = View.GONE
            }
        }
        viewModel.total.observe(viewLifecycleOwner) { total ->
            binding.txtTotal.text = "總金額：NT$$total"
        }
    }

    private fun updateCartUI(list: List<CartItem>) {
        val showEmpty = list.isEmpty() || !UserSession.isLogin
        val hasSelected = list.any { it.isSelected }

        binding.layoutEmpty.visibility =
            if (showEmpty) View.VISIBLE else View.GONE

        binding.recyclerCart.visibility =
            if (showEmpty) View.GONE else View.VISIBLE

        binding.layoutBottomBar.visibility =
            if (showEmpty) View.GONE else View.VISIBLE

        binding.btnCheckout.visibility =
            if (showEmpty) View.GONE else View.VISIBLE

        binding.btnDeleteSelected.visibility =
            if (hasSelected) View.VISIBLE else View.GONE
    }


    private fun checkout() {
        val list = viewModel.cartItems.value ?: return
        val selected = list.filter { it.isSelected }
        if (selected.isEmpty()) {
            Toast.makeText(requireContext(), "請先選擇商品", Toast.LENGTH_SHORT).show()
            return
        }
        val coupon = UserSession.selectedCoupon
        val order = OrderModel(
            items = selected.map {
                OrderItem(
                    it.productId,
                    it.productName,
                    it.price,
                    it.size,
                    it.quantity,
                    it.imageResId
                )
            },
            total = viewModel.total.value ?:0,
            discount = viewModel.discount.value ?:0,
            couponTitle = coupon?.title,
            fromCart = true,
            createdAt = System.currentTimeMillis()
        )

        findNavController()
            .navigate(CartFragmentDirections.actionCartToPayment(order))
    }

    private fun openProduct(productId: String) {
        val product = ProductDataSource.allProducts.find { it.id == productId }
            ?: return
        findNavController()
            .navigate(CartFragmentDirections.actionCartToProductDetail(product))
    }



    override fun onResume() {
        super.onResume()
        val main =(requireActivity() as MainActivity)
        main.showSimpleTitle("購物車")
        main.setFavoriteMenuVisible(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
