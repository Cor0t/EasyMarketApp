package com.example.easymarketapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.easymarketapp.R
import com.example.easymarketapp.model.Producto
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip

class ProductAdapter(
    private var products: List<Producto>,
    private val onProductClick: (Producto) -> Unit,
    private val onDeleteClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.productNameTextView)
        val brandTextView: TextView = view.findViewById(R.id.productBrandTextView)
        val priceTextView: TextView = view.findViewById(R.id.productPriceTextView)
        val categoryTextView: TextView = view.findViewById(R.id.categoryTextView)
        val productTagsTextView: Chip = view.findViewById(R.id.productTagsTextView)
        val productImageView: ImageView = view.findViewById(R.id.productImageView)
        val cantidadChip: Chip = view.findViewById(R.id.cantidadChip)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.nameTextView.text = product.nombre
        holder.brandTextView.text = product.marca
        holder.priceTextView.text = "$${String.format("%.0f", product.precio)}"

        // Mostrar categoría
        holder.categoryTextView.text = when(product.categoria) {
            "Leches" -> "🥛 Lácteos"
            "Pastas_Salsas" -> "🍝 Pastas y Salsas"
            "Arroz_Legumbres" -> "🌾 Arroz y Legumbres"
            "Helados" -> "🍨 Helados"
            else -> product.categoria
        }

        // Mostrar cantidad si es mayor a 1
        if (product.cantidad > 1) {
            holder.cantidadChip.visibility = View.VISIBLE
            holder.cantidadChip.text = "x${product.cantidad}"
        } else {
            holder.cantidadChip.visibility = View.GONE
        }

        // Mostrar tags especiales (Sin Lactosa/Sin Gluten)
        if (product.categoria == "Leches" &&
            product.nombre.lowercase().contains("sin lactosa")) {
            holder.productTagsTextView.visibility = View.VISIBLE
            holder.productTagsTextView.text = "Sin Lactosa"
        } else if (product.nombre.lowercase().contains("sin gluten")) {
            holder.productTagsTextView.visibility = View.VISIBLE
            holder.productTagsTextView.text = "Sin Gluten"
        } else {
            holder.productTagsTextView.visibility = View.GONE
        }

        // Cargar imagen
        Glide.with(holder.itemView.context)
            .load(product.imagen)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(holder.productImageView)

        // Configurar botón de eliminar
        holder.deleteButton.setOnClickListener {
            onDeleteClick(product)
        }

        holder.itemView.setOnClickListener { onProductClick(product) }
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Producto>) {
        products = newProducts
        notifyDataSetChanged()
    }
}