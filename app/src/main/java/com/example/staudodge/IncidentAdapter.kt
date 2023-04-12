package com.example.staudodge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.staudodge.enums.PriorityEnum

/**
 * The adapter and view holder class for Incidents
 */
class IncidentAdapter(private val incidentList: List<Incident>) : RecyclerView.Adapter<IncidentAdapter.IncidentViewHolder>(){

    class IncidentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val tvPriority : TextView = itemView.findViewById(R.id.tvPriority)
        val tvTitle : TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription : TextView = itemView.findViewById(R.id.tvDescription)
        val tvCategory : TextView = itemView.findViewById(R.id.tvCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.incident, parent, false)
        return IncidentViewHolder(view)
    }

    override fun getItemCount(): Int {
        return incidentList.size
    }

    //Priority need to be -1 because it starts at 1 while enums starts a 0
    override fun onBindViewHolder(holder: IncidentViewHolder, position: Int) {
        val incident = incidentList[position]
        holder.tvPriority.text = buildString {
            append(incident.priority.toString())
            append(" ")
            append(PriorityEnum.values()[incident.priority -1].priority)
    }
        holder.tvTitle.text = incident.title
        holder.tvDescription.text = incident.description
        holder.tvCategory.text = incident.category
    }

}