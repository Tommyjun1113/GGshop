package com.example.shopping.ui.home.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping.R
import com.example.shopping.ui.home.Product
import com.example.shopping.utils.FavoriteManager
import com.example.shopping.utils.UserSession


class ProductAdapter(
    private var items: List<Product>,
    private val onItemClick: (Product) -> Unit,
    private val onFavoriteClick: (Product) -> Unit,
    private val onRequireLogin: () -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.productImage)
        val name: TextView = itemView.findViewById(R.id.productName)
        val price: TextView = itemView.findViewById(R.id.productPrice)
        val imgFavorite: ImageButton = itemView.findViewById(R.id.productFavor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.img.setImageResource(item.imageResId[0])
        holder.name.text = item.name
        holder.price.text = "$${item.price}"

        if (!UserSession.isLogin) {
            holder.imgFavorite.visibility = View.VISIBLE
            holder.imgFavorite.setImageResource(R.drawable.heart)
            holder.imgFavorite.alpha = 1f
            holder.imgFavorite.setOnClickListener {
                onRequireLogin()
            }
        } else {
            holder.imgFavorite.visibility = View.VISIBLE
            item.isFavorite = FavoriteManager.isFavorite(context, item.id)
            holder.imgFavorite.setImageResource(
                if (item.isFavorite) R.drawable.favor else R.drawable.heart
            )
            holder.imgFavorite.setOnClickListener {
                onFavoriteClick(item)
            }
        }
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }
    override fun getItemCount() = items.size
    fun updateList(newItems: List<Product>) {
        items = newItems
        notifyDataSetChanged()
    }
}
