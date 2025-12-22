package com.example.shopping.ui.cart

import android.content.Intent
import android.graphics.Color
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

        binding.layoutCoupon.setOnClickListener {

            val selectedIds = viewModel.cartItems.value
                ?.filter { it.isSelected }
                ?.map { it.id }
                ?: emptyList()

            UserSession.preservedSelectedCartIds.clear()
            UserSession.preservedSelectedCartIds.addAll(selectedIds)

            findNavController().navigate(
                CartFragmentDirections.actionCartToNotifications()
            )
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

                override fun onSelectionChanged(item: CartItemUI, checked: Boolean) {
                    val domain = viewModel.cartItems.value
                        ?.firstOrNull { it.id == item.id }
                        ?: return
                    viewModel.updateSelection(domain, checked)
                }

                override fun increase(item: CartItemUI) {
                    val domain = viewModel.cartItems.value
                        ?.firstOrNull { it.id == item.id }
                        ?: return
                    viewModel.updateQty(domain, domain.quantity + 1)
                }

                override fun decrease(item: CartItemUI) {
                    val domain = viewModel.cartItems.value
                        ?.firstOrNull { it.id == item.id }
                        ?: return
                    if (domain.quantity > 1) {
                        viewModel.updateQty(domain, domain.quantity - 1)
                    }
                }

                override fun delete(item: CartItemUI) {
                    val domain = viewModel.cartItems.value
                        ?.firstOrNull { it.id == item.id }
                        ?: return
                    viewModel.deleteItem(domain)
                }

                override fun onItemClick(item: CartItemUI) {
                    openProduct(item.productId)
                }
            }
        )

        binding.recyclerCart.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCart.adapter = adapter
    }


    private fun updateCouponUI(list: List<CartItem>) {
        val coupon = UserSession.selectedCoupon

        val hasSelected = list.any { it.isSelected }
        val total = list.filter { it.isSelected }.sumOf { it.subtotal }

        val canUse = coupon != null && total >= coupon.minSpend
        val enabled = UserSession.isCouponEnabled


        binding.switchUseCoupon.setOnCheckedChangeListener(null)


        binding.switchUseCoupon.isEnabled = hasSelected
        binding.switchUseCoupon.isChecked = enabled && canUse

        binding.switchUseCoupon.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked && !canUse) {
                binding.switchUseCoupon.isChecked = false
                Toast.makeText(
                    requireContext(),
                    "尚未達優惠使用門檻",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnCheckedChangeListener
            }

            UserSession.isCouponEnabled = isChecked

            if (!isChecked) {
                UserSession.hasAutoAppliedCoupon = true
            }
            viewModel.calculateTotal(list)
        }


        when {
            coupon == null || !hasSelected -> {
                binding.txtCouponHint.visibility = View.GONE
            }

            !canUse -> {
                val diff = coupon.minSpend - total
                binding.txtCouponHint.text = "還差 NT$$diff 可使用此優惠（點我補差）"
                binding.txtCouponHint.setTextColor(Color.parseColor("#F2994A"))
                binding.txtCouponHint.visibility = View.VISIBLE


                UserSession.needMoreAmount = diff
                binding.txtCouponHint.setOnClickListener {
                    val selectedIds = viewModel.cartItems.value
                        ?.filter { it.isSelected }
                        ?.map { it.id }
                        ?: emptyList()

                    UserSession.preservedSelectedCartIds.clear()
                    UserSession.preservedSelectedCartIds.addAll(selectedIds)

                    UserSession.isRecommendForCoupon = true

                    findNavController().navigate(
                        CartFragmentDirections.actionCartToHome()
                    )
                }

            }

            else -> {
                binding.txtCouponHint.text = "✓ 已符合使用條件"
                binding.txtCouponHint.setTextColor(Color.parseColor("#2E7D32"))
                binding.txtCouponHint.visibility = View.VISIBLE
                binding.txtCouponHint.setOnClickListener(null)
                UserSession.needMoreAmount = 0
            }
        }


        binding.txtCoupon.text = when {
            !hasSelected ->
                "🎟 選擇優惠券"

            coupon == null ->
                "🎟 選擇優惠券"

            !canUse ->
                "🎟 已選優惠：${coupon.title}（未達門檻）"

            enabled ->
                "🎟 已套用優惠：${coupon.title}"

            else ->
                "🎟 已選優惠：${coupon.title}"
        }

        binding.txtCoupon.alpha = if (hasSelected) 1f else 0.6f
    }




    private fun observeViewModel() {
        viewModel.cartItemsUI.observe(viewLifecycleOwner) { uiList ->
            adapter.updateList(uiList )
        }
        viewModel.cartItems.observe(viewLifecycleOwner){ list ->

            UserSession.preservedSelectedCartIds.forEach { id ->
                viewModel.selectItemById(id)
            }

            UserSession.pendingSelectCartId?.let { newId ->
                viewModel.selectItemById(newId)
                UserSession.pendingSelectCartId = null
            }

            UserSession.preservedSelectedCartIds.clear()

            updateCartUI(list)
            updateCouponUI(list)
        }
        viewModel.discount.observe(viewLifecycleOwner) { discount ->
            val list = viewModel.cartItems.value ?: emptyList()
            updateCouponUI(list)
            if(discount > 0 && UserSession.isCouponEnabled){
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

        binding.layoutCoupon.visibility =
            if (!showEmpty && hasSelected) View.VISIBLE else View.GONE

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
                    it.imageRes.toString()
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
        val list = viewModel.cartItems.value?: emptyList()
        updateCouponUI(list)
        val main =(requireActivity() as MainActivity)
        main.showSimpleTitle("購物車")
        main.setFavoriteMenuVisible(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
