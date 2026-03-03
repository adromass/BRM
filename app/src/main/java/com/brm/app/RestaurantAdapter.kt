package com.brm.app

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// 1. Cambiamos List a MutableList para poder actualizar la lista
class RestaurantAdapter(private val restaurantList: MutableList<Restaurant>) :
    RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvRestaurantNameItem)
        val tvDetails: TextView = itemView.findViewById(R.id.tvRestaurantDetailsItem)
        val ivRestaurant: ImageView = itemView.findViewById(R.id.ivRestaurantItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = restaurantList[position]

        holder.tvName.text = restaurant.name
        holder.tvDetails.text = "${restaurant.address} | ⭐ ${restaurant.rating}"

        // 2. MODIFICACIÓN PARA FOTOS REALES (Bitmap)
        if (restaurant.bitmap != null) {
            // Si ya tenemos el Bitmap cargado, lo mostramos
            Glide.with(holder.itemView.context)
                .load(restaurant.bitmap)
                .into(holder.ivRestaurant)
        } else {
            // Si no, mostramos un placeholder mientras carga
            Glide.with(holder.itemView.context)
                .load(R.drawable.ic_launcher_background) // Usa tu placeholder aquí
                .into(holder.ivRestaurant)
        }

        holder.itemView.setOnClickListener {
            // Lógica al hacer clic
        }
    }

    override fun getItemCount(): Int = restaurantList.size

    // 3. FUNCIÓN PARA ACTUALIZAR UNA FOTO ESPECÍFICA (La que llamaremos desde Activity)
    fun updateRestaurantPhoto(restaurantId: String, bitmap: Bitmap) {
        val index = restaurantList.indexOfFirst { it.id == restaurantId }
        if (index != -1) {
            restaurantList[index].bitmap = bitmap
            notifyItemChanged(index)
        }
    }
}