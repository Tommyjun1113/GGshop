//package com.example.shopping.ui.user.adapter
//
//import android.annotation.SuppressLint
//import android.view.LayoutInflater
//import android.view.MotionEvent
//import android.view.View
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.example.shopping.R
//import com.example.shopping.ui.home.Product
//import android.widget.ImageView
//import android.widget.TextView
//import com.bumptech.glide.Glide
//
//class ProductAdapter(
//    private val items: List<Product>,
//    private val onItemClick: (Product) -> Unit
//) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
//
//    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val img = itemView.findViewById<ImageView>(R.id.productImage)
//        val name = itemView.findViewById<TextView>(R.id.productName)
//        val price = itemView.findViewById<TextView>(R.id.productPrice)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_product, parent, false)
//        return ProductViewHolder(view)
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
//        val item = items[position]
//
//        holder.itemView.apply {
//            val resId = context.resources.getIdentifier(
//                item.imageKey,
//                "drawable",
//                context.packageName
//            )
//
//            if (resId != 0) {
//                item.setImageResource(resId)
//            } else {
//                item.setImageResource(R.drawable.ggicon_1)
//            }
//        }
//
//
//        holder.name.text = item.name
//        holder.price.text = "$${item.price}"
//
//
//        holder.itemView.setOnClickListener {
//            onItemClick(item)
//        }
//
//
//        holder.itemView.setOnTouchListener { v, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    v.animate()
//                        .scaleX(1.05f)
//                        .scaleY(1.05f)
//                        .setDuration(120)
//                        .start()
//                }
//                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                    v.animate()
//                        .scaleX(1f)
//                        .scaleY(1f)
//                        .setDuration(120)
//                        .start()
//                }
//            }
//            false
//        }
//    }
//
//    override fun getItemCount() = items.size
//}
