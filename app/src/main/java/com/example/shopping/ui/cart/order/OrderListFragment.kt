import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopping.databinding.FragmentOrderListBinding
import com.example.shopping.ui.cart.adapter.OrderListAdapter
import com.example.shopping.ui.cart.order.OrderModel
import com.example.shopping.utils.UserSession
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OrderListFragment : Fragment() {

    private lateinit var binding: FragmentOrderListBinding
    private val orderList = mutableListOf<OrderModel>()
    private lateinit var adapter: OrderListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOrderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OrderListAdapter(orderList)
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
                    orderList.add(
                        OrderModel(
                            id = doc.id,
                            total = (doc.getLong("total") ?: 0).toInt(),
                            createdAt = doc.getLong("createdAt") ?: 0
                        )
                    )
                }

                adapter.notifyDataSetChanged()
            }
    }
}
