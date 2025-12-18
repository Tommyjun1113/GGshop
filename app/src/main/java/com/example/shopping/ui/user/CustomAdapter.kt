package com.example.shopping.ui.user

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping.R
class CustomAdapter(private var dataSet: List<UserItem>,
    private val onItemClick:(Int) -> Unit) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder)
         */
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val usertv: TextView =view.findViewById(R.id.usertv)
            val usertv2: TextView = view.findViewById(R.id.usertv2)

        }
        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            // Create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.user, viewGroup, false)
            return ViewHolder(view)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.usertv.text=dataSet[position].usertv
            viewHolder.usertv2.text=dataSet[position].usertv2

            viewHolder.itemView.setOnClickListener {
                onItemClick(position)
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = dataSet.size
    }