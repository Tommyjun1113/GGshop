package com.example.shopping.ui.user.order

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
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

        binding.btnApplyReturn.setOnClickListener {
            showReturnDialog()
        }

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
                "LINEPAY", "LINE Pay" -> R.drawable.img_8
                else -> R.drawable.img_6
            }
            binding.imgPaymentIcon.setImageResource(iconRes)
        }

        viewModel.createdAt.observe(viewLifecycleOwner) {
            binding.txtDetailTime.text =
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
                    .format(java.util.Date(it))
        }
        viewModel.orderStatus.observe(viewLifecycleOwner) {
            updateOrderStatus(it)
        }

        viewModel.returnReason.observe(viewLifecycleOwner) {
            if (!it.isNullOrBlank()) {
                binding.txtReturnInfo.text = "退貨原因：$it"
            }
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
    private fun updateOrderStatus(status: String) {
        when (status) {

            "PENDING" -> {
                binding.txtOrderStatus.text = "訂單狀態：待付款 / 處理中"
                binding.txtOrderStatus.setTextColor(Color.parseColor("#2d2e2d"))

                binding.layoutReturn.visibility = View.VISIBLE
                binding.btnApplyReturn.visibility = View.VISIBLE
                binding.txtReturnInfo.visibility = View.GONE
            }
            "COMPLETED" -> {
                binding.txtOrderStatus.text = "訂單狀態：已完成"
                binding.txtOrderStatus.setTextColor(Color.parseColor("#27AE60"))

                binding.layoutReturn.visibility = View.VISIBLE
                binding.btnApplyReturn.visibility = View.VISIBLE
                binding.txtReturnInfo.visibility = View.GONE
            }

            "RETURN_REQUESTED" -> {
                binding.txtOrderStatus.text = "訂單狀態：退貨申請中"
                binding.txtOrderStatus.setTextColor(Color.parseColor("#c7c183"))

                binding.layoutReturn.visibility = View.VISIBLE
                binding.btnApplyReturn.visibility = View.GONE
                binding.txtReturnInfo.visibility = View.VISIBLE
            }

            "RETURN_APPROVED" -> {
                binding.txtOrderStatus.text = "訂單狀態：退貨審核通過"
                binding.txtOrderStatus.setTextColor(Color.parseColor("#ed7a6f"))

                binding.layoutReturn.visibility = View.GONE
            }

            "RETURN_REJECTED" -> {
                binding.txtOrderStatus.text = "訂單狀態：退貨被拒"
                binding.txtOrderStatus.setTextColor(Color.RED)

                binding.layoutReturn.visibility = View.GONE
            }

            "RETURN_COMPLETED" -> {
                binding.txtOrderStatus.text = "訂單狀態：退貨完成"
                binding.txtOrderStatus.setTextColor(Color.GRAY)

                binding.layoutReturn.visibility = View.GONE
            }
        }
    }
    private fun showReturnDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_return, null)

        val edtReason = dialogView.findViewById<EditText>(R.id.edtReturnReason)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val btnSubmit = dialogView.findViewById<TextView>(R.id.btnSubmit)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSubmit.setOnClickListener {
            val reason = edtReason.text.toString().trim()
            if (reason.isBlank()) {
                edtReason.error = "請輸入退貨原因"
                return@setOnClickListener
            }
            submitReturn(reason)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun submitReturn(reason: String) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(UserSession.documentId)
            .collection("orders")
            .document(orderId)
            .update(
                mapOf(
                    "status" to "RETURN_REQUESTED",
                    "return" to mapOf(
                        "reason" to reason,
                        "note" to "",
                        "createdAt" to System.currentTimeMillis()
                    )
                )
            )
            .addOnSuccessListener {
                android.widget.Toast
                    .makeText(requireContext(), "退貨申請已送出", android.widget.Toast.LENGTH_SHORT)
                    .show()

                viewModel.loadOrderDetail(orderId)
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
