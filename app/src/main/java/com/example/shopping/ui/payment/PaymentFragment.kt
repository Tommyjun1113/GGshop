package com.example.shopping.ui.payment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.wear.compose.material.Vignette
import com.example.shopping.R
import com.example.shopping.databinding.FragmentPaymentBinding
import com.example.shopping.ui.main.MainActivity
import com.example.shopping.ui.payment.adapter.PaymentItemAdapter
import com.example.shopping.ui.user.adapter.OrderDetailAdapter
import com.example.shopping.ui.user.order.OrderModel
import com.example.shopping.utils.UserSession
import com.google.firebase.firestore.FirebaseFirestore

class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private lateinit var order: OrderModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        order = PaymentFragmentArgs.fromBundle(requireArguments()).order
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "付款方式"
            setDisplayHomeAsUpEnabled(true)
        }

        showOrderPreview()
        setupCardInput()
        setupPaymentListener()

    }

    private fun showOrderPreview() {
        binding.recyclerOrderItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = PaymentItemAdapter(order.items)
        }
        binding.txtPayTotal.text = "應付金額：NT$${order.total}"

        if(order.discount > 0 && !order.couponTitle.isNullOrBlank()){
            binding.txtCouponInfo.visibility = View.VISIBLE
            binding.txtCouponInfo.text = "已使用優惠 :${order.couponTitle} (-NT$${ order.discount})"
        }else{
            binding.txtCouponInfo.visibility = View.GONE
        }
    }

    private fun setupCardInput() {
        binding.layoutCard.visibility = View.GONE

        binding.edtCardNumber.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true
                val digits = s.toString().replace("-", "")
                val formatted = digits.chunked(4).joinToString("-")
                s?.replace(0, s.length, formatted)
                isFormatting = false
            }
        })
    }

    private fun setupPaymentListener() {
        binding.layoutCard.visibility = View.GONE
        binding.layoutCash.visibility = View.GONE

        binding.radioGroupPayment.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioCreditCard -> {
                    binding.layoutCard.visibility = View.VISIBLE
                    binding.layoutCash.visibility = View.GONE
                }

                R.id.radioCash -> {
                    binding.layoutCard.visibility = View.GONE
                    binding.layoutCash.visibility = View.VISIBLE
                }

                R.id.radioLinePay -> {
                    binding.layoutCard.visibility = View.GONE
                    binding.layoutCash.visibility = View.GONE
                }
            }
        }

        binding.btnConfirmPay.setOnClickListener {
            binding.btnConfirmPay.isEnabled = false

            val checkedId = binding.radioGroupPayment.checkedRadioButtonId
            if (checkedId == -1) {
                Toast.makeText(requireContext(), "請選擇付款方式", Toast.LENGTH_SHORT).show()
                binding.btnConfirmPay.isEnabled = true
                return@setOnClickListener
            }
            when (checkedId) {
                R.id.radioCreditCard -> {

                    if (binding.edtCardNumber.text.isNullOrBlank()
                        || binding.edtCardTime.text.isNullOrBlank()
                        || binding.edtCardCVV.text.isNullOrBlank()
                    ) {
                        Toast.makeText(requireContext(), "請完整輸入信用卡資訊", Toast.LENGTH_SHORT).show()
                        binding.btnConfirmPay.isEnabled = true
                        return@setOnClickListener
                    }
                }

                R.id.radioCash -> {

                    if (binding.edtReceiverName.text.isNullOrBlank()
                        || binding.edtReceiverPhone.text.isNullOrBlank()
                        || binding.edtReceiverAddress.text.isNullOrBlank()
                    ) {
                        Toast.makeText(requireContext(), "請完整輸入收件資訊", Toast.LENGTH_SHORT).show()
                        binding.btnConfirmPay.isEnabled = true
                        return@setOnClickListener
                    }
                }

                R.id.radioLinePay -> {

                }
            }

            createOrder()
        }
    }


    private fun createOrder() {
        val userId = UserSession.documentId
        val db = FirebaseFirestore.getInstance()
        val coupon = UserSession.selectedCoupon

        val paymentMethod = when {
            binding.radioCreditCard.isChecked -> "信用卡"
            binding.radioCash.isChecked -> "貨到付款"
            else -> "LINE Pay"
        }

        val orderData = mapOf(
            "items" to order.items,
            "total" to order.total,
            "discount" to order.discount,
            "couponId" to coupon?.id,
            "couponTitle" to coupon?.title,
            "paymentMethod" to paymentMethod,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(userId)
            .collection("orders")
            .add(orderData)
            .addOnSuccessListener { doc ->

                consumeCouponIfNeeded{
                    if (order.fromCart) clearCart { goPaySuccess(doc.id) } else goPaySuccess(doc.id)
                }
            }
    }
    private fun consumeCouponIfNeeded(onDone: () -> Unit) {
        val coupon = UserSession.selectedCoupon
        if (coupon == null) {
            onDone()
            return
        }

        val userId = UserSession.documentId
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(userId)
            .collection("coupons")
            .document(coupon.id)
            .update(
                mapOf(
                    "used" to true,
                    "usedAt" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener {
                UserSession.selectedCoupon = null
                onDone()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(),"優惠券使用失敗", Toast.LENGTH_SHORT).show()
                onDone()
            }
    }

    private fun clearCart(done: () -> Unit) {
        val userId = UserSession.documentId
        val db = FirebaseFirestore.getInstance()
        val cartRef = db.collection("users")
            .document(userId)
            .collection("cart")

        val batch = db.batch()

        var pendingQueries = order.items.size
        if (pendingQueries == 0) {
            done()
            return
        }

        order.items.forEach { item ->
            cartRef
                .whereEqualTo("productId", item.productId)
                .whereEqualTo("size", item.size)
                .get()
                .addOnSuccessListener { query ->

                    query.documents.forEach { doc ->
                        batch.delete(doc.reference)
                    }

                    pendingQueries--
                    if (pendingQueries == 0) {
                        batch.commit().addOnSuccessListener { done() }
                    }
                }
        }
    }

    private fun goPaySuccess(orderId: String) {
        findNavController().navigate(
            PaymentFragmentDirections.actionPaymentToPaySuccess(orderId)
        )
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
        main.showProductToolbar("付款方式")
        main.hideProductActionBar()
        main.setFavoriteMenuVisible(false)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

