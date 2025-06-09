package com.rexensoft.mybus.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rexensoft.mybus.R
import com.rexensoft.mybus.data.model.BusModel

class BusAdapter(
    private val buses: List<BusModel>,
    private val onBusClick: (BusModel) -> Unit
) : RecyclerView.Adapter<BusAdapter.BusViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bus, parent, false)
        return BusViewHolder(view)
    }

    override fun onBindViewHolder(holder: BusViewHolder, position: Int) {
        val bus = buses[position]
        holder.bind(bus)
        holder.itemView.setOnClickListener { onBusClick(bus) }
    }

    override fun getItemCount(): Int = buses.size

    class BusViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvBusId: TextView = view.findViewById(R.id.tvBusId)
        private val tvCoordinates: TextView = view.findViewById(R.id.tvCoordinates)

        fun bind(bus: BusModel) {
            tvBusId.text = bus.id
            tvCoordinates.text = "Lat: ${bus.latitude}, Lng: ${bus.longitude}"
        }
    }
}
