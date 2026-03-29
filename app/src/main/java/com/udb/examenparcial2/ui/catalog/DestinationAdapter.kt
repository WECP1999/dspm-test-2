package com.udb.examenparcial2.ui.catalog

import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.udb.examenparcial2.R
import com.udb.examenparcial2.data.model.Destination
import com.udb.examenparcial2.databinding.ItemDestinationBinding
import java.util.Locale

class DestinationAdapter(private val onItemClick: (Destination) -> Unit) :
    ListAdapter<Destination, DestinationAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDestinationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemDestinationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(destination: Destination) {
            binding.apply {
                tvDestinationName.text = "${destination.name}, ${destination.country}"
                tvDestinationPrice.text = String.format(Locale.US, "$%.2f", destination.price)
                tvDestinationDescription.text = destination.description

                val imageSource: Any = if (destination.imageUrl.length > 200) {
                    try {
                        Base64.decode(destination.imageUrl, Base64.DEFAULT)
                    } catch (e: Exception) {
                        destination.imageUrl
                    }
                } else {
                    destination.imageUrl
                }

                Glide.with(ivDestination.context)
                    .load(imageSource)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(ivDestination)

                root.setOnClickListener { onItemClick(destination) }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Destination>() {
        override fun areItemsTheSame(oldItem: Destination, newItem: Destination): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Destination, newItem: Destination): Boolean {
            return oldItem == newItem
        }
    }
}
