package com.example.shopping.ui.user

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopping.R
import com.example.shopping.databinding.FragmentPurchaseBinding
import com.example.shopping.ui.main.MainActivity
import com.example.shopping.ui.user.adapter.OrderListAdapter
import com.example.shopping.ui.user.order.OrderItem
import com.example.shopping.ui.user.order.OrderModel
import com.example.shopping.utils.UserSession
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Purchase : Fragment() {

    private var _binding: FragmentPurchaseBinding? = null
    private val binding get() = _binding!!

    private val orderList = mutableListOf<OrderModel>()
    private lateinit var adapter: OrderListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPurchaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        (activity as AppCompatActivity).supportActionBar?.apply {
            title = "購買紀錄"
            setDisplayHomeAsUpEnabled(true)
        }

        adapter = OrderListAdapter(orderList) { order ->

            val action = PurchaseDirections.actionPurchaseToOrderDetail(order.id)
            findNavController().navigate(action)
        }

        binding.recyclerOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerOrders.adapter = adapter

        loadOrders()
    }

    private fun loadOrders() {
        val userId = UserSession.documentId

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("orders")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->

                orderList.clear()

                for (doc in result) {
                    val rawItems =
                        doc.get("items") as? List<Map<String, Any>> ?: emptyList()

                    val orderItems = rawItems.map {
                        OrderItem(
                            productId = it["productId"] as String,
                            productName = it["productName"] as String,
                            price = (it["price"] as Long).toInt(),
                            size = it["size"] as String,
                            quantity = (it["quantity"] as Long).toInt(),
                            imageResId = (it["imageResId"] as Long).toInt()
                        )
                    }

                    orderList.add(
                        OrderModel(
                            id = doc.id,
                            total = (doc.getLong("total") ?: 0).toInt(),
                            createdAt = doc.getLong("createdAt") ?: 0L,
                            items = orderItems,
                            paymentMethod = doc.getString("paymentMethod") ?: ""
                        )
                    )
                }

                adapter.notifyDataSetChanged()

                binding.txtEmptyOrders.visibility =
                    if (orderList.isEmpty()) View.VISIBLE else View.GONE
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
        main.showProductToolbar("購買紀錄")
        main.hideProductActionBar()

    }


    override fun onStop() {
        super.onStop()
        requireActivity().findViewById<View>(R.id.nav_view)?.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
}
