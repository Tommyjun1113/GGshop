package com.example.shopping.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.wear.compose.material.Vignette
import com.example.shopping.R
import com.example.shopping.databinding.FragmentNotificationsBinding
import com.example.shopping.ui.main.MainActivity
import com.example.shopping.ui.notifications.adapter.NotificationAdapter
import com.example.shopping.utils.UserSession
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    private val binding get() = _binding!!
    private var allCoupons: List<NotificationItem> = emptyList()
    private lateinit var viewModel: NotificationsViewModel
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]
        setupRecyclerView()
        observeViewModel()
        updateLoginUI()

        binding.btnGoShopping.setOnClickListener {
            (requireActivity() as MainActivity)
                .binding
                .navView
                .selectedItemId = R.id.navigation_home
        }
        binding.tabCoupon.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {
                when(tab.position){
                    0 -> showCoupons(CouponStatus.AVAILABLE)
                    1 -> showCoupons(CouponStatus.USED)
                    2 -> showCoupons(CouponStatus.EXPIRED)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })



        return binding.root
    }
    private fun setupRecyclerView() {
        adapter = NotificationAdapter(emptyList()){ item ->
            findNavController().navigate(
                NotificationsFragmentDirections
                    .actionNotificationsToCouponDetail(item.coupon)
            )
            Toast.makeText(
                requireContext(),
                "點擊優惠 : ${item.coupon.title}",
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.recyclerNotifications.layoutManager =
            LinearLayoutManager(requireContext())
        binding.recyclerNotifications.adapter = adapter
    }
    private fun observeViewModel() {
        viewModel.notifications.observe(viewLifecycleOwner) { list ->


            allCoupons = list


            showCoupons(CouponStatus.AVAILABLE)
        }
    }
    private fun showCoupons(status: CouponStatus) {
        val filtered = allCoupons.filter { it.status == status }

        if (filtered.isEmpty()) {
            binding.layoutEmptyCoupon.visibility = View.VISIBLE
            binding.recyclerNotifications.visibility = View.GONE

            binding.txtEmptyCoupon.text = when (status) {
                CouponStatus.AVAILABLE -> "目前沒有可使用的優惠券"
                CouponStatus.USED -> "尚無已使用的優惠券"
                CouponStatus.EXPIRED -> "尚無已過期的優惠券"
            }
        } else {
            binding.layoutEmptyCoupon.visibility = View.GONE
            binding.recyclerNotifications.visibility = View.VISIBLE
            adapter.updateList(filtered)
        }
    }

    private fun updateLoginUI() {
        if (!UserSession.isLogin) {
            binding.txtNeedLogin.visibility = View.VISIBLE
            binding.tabCoupon.visibility = View.GONE
            binding.recyclerNotifications.visibility = View.GONE
            binding.layoutEmptyCoupon.visibility = View.GONE
        } else {
            binding.txtNeedLogin.visibility = View.GONE
            binding.tabCoupon.visibility = View.VISIBLE
            viewModel.loadNotifications(UserSession.documentId)
        }
    }

    override fun onResume() {
        super.onResume()
        updateLoginUI()

        val main = requireActivity() as MainActivity
        main.showSimpleTitle("通知")
        main.hideProductActionBar()
        main.setFavoriteMenuVisible(false)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}