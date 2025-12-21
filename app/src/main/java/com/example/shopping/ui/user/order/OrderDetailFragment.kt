package com.example.shopping.ui.user.order

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopping.R
import com.example.shopping.databinding.FragmentOrderDetailBinding
import com.example.shopping.ui.main.MainActivity
import com.example.shopping.ui.user.adapter.OrderDetailAdapter
import com.example.shopping.utils.UserSession
import com.google.firebase.firestore.FirebaseFirestore

class OrderDetailFragment : Fragment() {

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var orderId: String

    private val viewModel: OrderDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        orderId = OrderDetailFragmentArgs.fromBundle(requireArguments()).orderId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.apply {
            title = "訂單明細"
            setDisplayHomeAsUpEnabled(true)
        }

        binding.recyclerOrderDetail.layoutManager =
            LinearLayoutManager(requireContext())
        observeViewModel()
        viewModel.loadOrderDetail(orderId)
    }

    private fun observeViewModel() {

        viewModel.items.observe(viewLifecycleOwner) {
            binding.recyclerOrderDetail.adapter = OrderDetailAdapter(it)
        }

        viewModel.total.observe(viewLifecycleOwner) {
            binding.txtDetailTotal.text = "總金額：NT$$it"
        }

        viewModel.discount.observe(viewLifecycleOwner) {
            updateCouponInfo()
        }

        viewModel.couponTitle.observe(viewLifecycleOwner) {
            updateCouponInfo()
        }

        viewModel.paymentMethod.observe(viewLifecycleOwner) {
            binding.txtPaymentMethod.text = "付款方式：$it"

            val iconRes = when (it) {
                "信用卡" -> R.drawable.img_6
                "貨到付款" -> R.drawable.img_7
                "LINE Pay" -> R.drawable.img_8
                else -> R.drawable.img_6
            }
            binding.imgPaymentIcon.setImageResource(iconRes)
        }

        viewModel.createdAt.observe(viewLifecycleOwner) {
            binding.txtDetailTime.text =
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
                    .format(java.util.Date(it))
        }
    }
    private fun updateCouponInfo() {
        val discount = viewModel.discount.value ?: 0
        val title = viewModel.couponTitle.value

        if (discount > 0 && !title.isNullOrBlank()) {
            binding.txtCouponInfo.visibility = View.VISIBLE
            binding.txtCouponInfo.text =
                "使用優惠券：$title\n折扣金額：-NT$$discount"
        } else {
            binding.txtCouponInfo.visibility = View.GONE
        }
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
        main.showProductToolbar("訂單明細")
        main.hideProductActionBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
}
