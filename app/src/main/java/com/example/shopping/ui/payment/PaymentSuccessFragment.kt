package com.example.shopping.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.shopping.R
import com.example.shopping.ui.main.MainActivity


class PaymentSuccessFragment : Fragment() {

    private lateinit var orderId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orderId = PaymentSuccessFragmentArgs
            .fromBundle(requireArguments())
            .orderId
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_payment_success, container, false)

        view.findViewById<Button>(R.id.btnViewOrder).setOnClickListener {
            val action =
                PaymentSuccessFragmentDirections
                    .actionPaySuccessToOrderDetail(orderId)

            findNavController().navigate(action)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        val main = requireActivity() as MainActivity
        main.setFavoriteMenuVisible(false)
    }
}
