package com.example.easymarketapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.easymarketapp.R
import com.example.easymarketapp.model.Producto
import com.bumptech.glide.Glide

class ProductAdapter(
    private var productos: List<Producto>,
    private val onProductClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.productNameTextView)
        val brandTextView: TextView = view.findViewById(R.id.productBrandTextView)
        val priceTextView: TextView = view.findViewById(R.id.productPriceTextView)
        val tagsTextView: TextView = view.findViewById(R.id.productTagsTextView)
        val productImageView: ImageView = view.findViewById(R.id.productImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productos[position]

        holder.nameTextView.text = product.nombre
        holder.brandTextView.text = product.marca
        holder.priceTextView.text = "$${String.format("%.0f", product.precio)}"

        // Mostrar etiqueta de sin lactosa si aplica
        holder.tagsTextView.text = if (product.islactoseIntolerant) "Sin Lactosa" else ""

        // Cargar imagen usando Glide
        Glide.with(holder.itemView.context)
            .load(product.imagen)
            .placeholder(R.drawable.placeholder_image) // Asegúrate de tener esta imagen
            .error(R.drawable.error_image) // Asegúrate de tener esta imagen
            .into(holder.productImageView)

        holder.itemView.setOnClickListener { onProductClick(product) }
    }

    override fun getItemCount() = productos.size

    fun updateProducts(newProductos: List<Producto>) {
        productos = newProductos
        notifyDataSetChanged()
    }
}