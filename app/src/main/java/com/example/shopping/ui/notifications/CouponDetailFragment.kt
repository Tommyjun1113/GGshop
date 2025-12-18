package com.example.shopping.ui.notifications

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.shopping.databinding.FragmentCouponDetailBinding
import com.example.shopping.ui.main.MainActivity
import com.example.shopping.utils.UserSession

class CouponDetailFragment : Fragment() {

    private var _binding: FragmentCouponDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var coupon: NotificationItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        coupon = CouponDetailFragmentArgs
            .fromBundle(requireArguments())
            .coupon
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCouponDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.txtTitle.text = coupon.title
        binding.txtExpire.text = "有效期限：${coupon.expireDate}"
        binding.txtRule.text = """
            • 僅限指定商品使用
            • 不可與其他優惠併用
            • 每筆訂單限用一次
        """.trimIndent()

        binding.btnUse.setOnClickListener {
            goToCart()
        }
    }

    private fun goToCart() {
        UserSession.selectedCoupon = coupon

        findNavController().navigate(
            CouponDetailFragmentDirections
                .actionCouponDetailToCart()
        )
    }

    override fun onResume() {
        super.onResume()
        val main = requireActivity() as MainActivity
        main.showSimpleTitle("通知")
        main.hideProductActionBar()
        main.setFavoriteMenuVisible(true)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
