package com.example.easymarketapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.easymarketapp.R
import com.example.easymarketapp.model.Producto
import java.util.Locale

class ProductosSimilaresAdapter :
    ListAdapter<Producto, ProductosSimilaresAdapter.ViewHolder>(ProductoDiffCallback()) {

    private val selectedQuantities = mutableMapOf<String, Int>()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.productImageView)
        val nameText: TextView = view.findViewById(R.id.productNameTextView)
        val brandText: TextView = view.findViewById(R.id.productBrandTextView)
        val priceText: TextView = view.findViewById(R.id.productPriceTextView)
        val quantityText: TextView = view.findViewById(R.id.quantityTextView)
        val decreaseButton: ImageButton = view.findViewById(R.id.decreaseButton)
        val increaseButton: ImageButton = view.findViewById(R.id.increaseButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_similar, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val producto = getItem(position)
        val quantity = selectedQuantities[producto.sku] ?: 0

        with(holder) {
            nameText.text = producto.nombre
            brandText.text = producto.marca
            priceText.text = holder.itemView.context.getString(
                R.string.price_format,
                String.format(Locale.getDefault(), "%,.0f", producto.precio)
            )
            quantityText.text = quantity.toString()

            // Cargar imagen con Glide
            Glide.with(holder.itemView.context)
                .load(producto.imagen)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(imageView)

            decreaseButton.isEnabled = quantity > 0
            decreaseButton.alpha = if (quantity > 0) 1f else 0.5f

            decreaseButton.setOnClickListener {
                if (quantity > 0) {
                    selectedQuantities[producto.sku] = quantity - 1
                    notifyItemChanged(position)
                }
            }

            increaseButton.setOnClickListener {
                selectedQuantities[producto.sku] = quantity + 1
                notifyItemChanged(position)
            }
        }
    }

    fun getSelectedProducts(): List<Producto> {
        return currentList.filter { producto ->
            selectedQuantities[producto.sku]?.let { it > 0 } ?: false
        }.map { producto ->
            producto.copy(cantidad = selectedQuantities[producto.sku] ?: 0)
        }
    }

    private class ProductoDiffCallback : DiffUtil.ItemCallback<Producto>() {
        override fun areItemsTheSame(oldItem: Producto, newItem: Producto) =
            oldItem.sku == newItem.sku

        override fun areContentsTheSame(oldItem: Producto, newItem: Producto) =
            oldItem == newItem
    }
}