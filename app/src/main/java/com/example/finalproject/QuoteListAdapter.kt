package com.example.finalproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QuoteListAdapter(private val quotes: MutableList<QuoteItem>) : RecyclerView.Adapter<QuoteListAdapter.QuoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quote, parent, false)
        return QuoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        val quote = quotes[position]
        holder.quoteText.text = quote.text

        // 如果語錄已抽取，設置文字顏色為黑色，否則為灰色
        holder.quoteText.setTextColor(
            holder.itemView.context.resources.getColor(
                if (quote.drawn) R.color.black else R.color.gray,
                null
            )
        )

        // 設置點擊事件
        holder.itemView.setOnClickListener {
            if (quote.drawn) {
                // 如果語錄已抽取，可以執行某些操作，例如顯示詳情
            } else {
                // 如果語錄未抽取，提示用戶需要先抽取
            }
        }
    }

    override fun getItemCount(): Int = quotes.size

    class QuoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val quoteText: TextView = itemView.findViewById(R.id.quoteText)
    }
}
