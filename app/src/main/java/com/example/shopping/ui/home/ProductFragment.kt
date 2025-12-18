package com.example.shopping.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shopping.R
import com.example.shopping.databinding.FragmentProductBinding
import com.example.shopping.ui.home.adapter.ThumbnailAdapter
import com.example.shopping.ui.login.LoginActivity
import com.example.shopping.ui.main.MainActivity
import com.example.shopping.ui.payment.PaymentFragment
import com.example.shopping.ui.user.order.OrderItem
import com.example.shopping.ui.user.order.OrderModel
import com.example.shopping.utils.FavoriteManager
import com.example.shopping.utils.UserSession
import com.google.firebase.firestore.FirebaseFirestore

class ProductFragment : Fragment() {

    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var product: Product

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        product =  ProductFragmentArgs.fromBundle(requireArguments()).product

        binding.apply {
            binding.productImage.setImageResource(product.imageResId[0])

            val adapter = ThumbnailAdapter(product.imageResId) { clickedUrl ->
                Glide.with(this@ProductFragment)
                    .load(clickedUrl)
                    .into(productImage)
            }

            recyclerThumbnails.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            recyclerThumbnails.adapter = adapter

            productName.text = product.name
            productPrice.text = "NT$${product.price}"
            productDescription.text = "商品資訊:\n${product.description}"
        }
        setupFavoriteButton()
        val activity = requireActivity() as MainActivity

        activity.btnAddToCart.setOnClickListener {
            AddToCartBottomSheet(product) { size, qty ->
                addToCart(
                    product.id,
                    product.name,
                    product.price,
                    size,
                    qty,
                    product.imageResId[0]
                )
            }.show(parentFragmentManager, "AddToCart")
        }


        activity.btnBuyNow.setOnClickListener {
            AddToCartBottomSheet(product) { size, qty ->
                buyNow(
                    product.id,
                    product.name,
                    product.price,
                    size,
                    qty,
                    product.imageResId[0]
                )
            }.show(parentFragmentManager, "BuyNow")
        }
    }

    private fun setupFavoriteButton(){
        val context = requireContext()
        fun refreshFavoriteIcon(){
            val isFavorite=
                UserSession.isLogin &&
                        FavoriteManager.isFavorite(context,product.id)
            binding.imageButton.setImageResource(
                if(isFavorite) R.drawable.favor else R.drawable.heart
            )
        }
        refreshFavoriteIcon()
        binding.imageButton.setOnClickListener {
            if(!UserSession.isLogin){
                Toast.makeText(context,"請先登入!!",Toast.LENGTH_SHORT).show()
                startActivity(Intent(context, LoginActivity::class.java))
                return@setOnClickListener
            }
            val newState = FavoriteManager.toggleFavorite(context,product.id)
            product.isFavorite = newState
            refreshFavoriteIcon()
        }
    }
    private fun addToCart(
        productId: String,
        productName: String,
        price: Int,
        size: String,
        quantity: Int,
        imageResId: Int,
    ) {
        val userId = UserSession.documentId

        if (userId.isEmpty()) {
            Toast.makeText(requireContext(), "請先登入", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            return
        }

        val cartRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("cart")

        cartRef
            .whereEqualTo("productId", productId)
            .whereEqualTo("size", size)
            .get()
            .addOnSuccessListener { query ->

                if (query.isEmpty) {

                    val item = hashMapOf(
                        "productId" to productId,
                        "productName" to productName,
                        "price" to price,
                        "size" to size,
                        "quantity" to quantity,
                        "imageResId" to imageResId,
                        "createdAt" to System.currentTimeMillis()
                    )

                    cartRef.add(item)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "成功加入購物車！$productName $size x $quantity", Toast.LENGTH_SHORT).show()
                        }

                } else {
                    val doc = query.documents[0]
                    val oldQty = doc.getLong("quantity")?.toInt() ?: 1
                    val newQty = oldQty + quantity

                    cartRef.document(doc.id)
                        .update("quantity", newQty)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "更新購物車數量！", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }
    private fun buyNow(
        productId: String,
        productName: String,
        price: Int,
        size: String,
        quantity: Int,
        imageResId: Int,
    ) {
        if (!UserSession.isLogin) {
            Toast.makeText(requireContext(), "請先登入", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            return
        }

        val order = OrderModel(
            items = listOf(
                OrderItem(
                    productId = productId,
                    productName = productName,
                    price = price,
                    size = size,
                    quantity = quantity,
                    imageResId = imageResId
                )
            ),
            total = price * quantity,
            fromCart = false,
            createdAt = System.currentTimeMillis()
        )

        val action =
            ProductFragmentDirections
                .actionProductDetailToPayment(order)

        findNavController().navigate(action)
    }



    override fun onResume() {
        super.onResume()
        val main = requireActivity() as MainActivity
        main.showProductToolbar(product.name)
        main.showProductActionBar()
        main.setFavoriteMenuVisible(true)
    }

    override fun onPause() {
        super.onPause()
        val main = requireActivity() as MainActivity
        main.hideProductActionBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
