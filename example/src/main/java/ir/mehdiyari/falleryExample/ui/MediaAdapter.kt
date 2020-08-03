package ir.mehdiyari.falleryExample.ui

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ir.mehdiyari.falleryExample.R
import kotlinx.android.synthetic.main.media_item_view.view.*

class MediaAdapter : ListAdapter<Pair<String, String>, MediaAdapter.MediaViewHolder>(object : DiffUtil.ItemCallback<Pair<String, String>>() {
    override fun areItemsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean = oldItem.hashCode() == newItem.hashCode()
    override fun areContentsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean = oldItem == newItem
}) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder = MediaViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.media_item_view, parent, false)
    )

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind()
    }

    inner class MediaViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind() {
            getItem(adapterPosition).apply {
                if (this.second.isEmpty()) {
                    itemView.captionContainer.visibility = View.GONE
                } else {
                    itemView.captionContainer.visibility = View.VISIBLE
                    itemView.appCompatTextViewCaption.text = this.second
                    itemView.appCompatTextViewCaption.isSelected = true
                }

                Glide.with(itemView.imageViewMediaPhoto)
                    .load(this.first)
                    .placeholder(ColorDrawable(ContextCompat.getColor(itemView.context, R.color.blue_600)))
                    .centerCrop()
                    .into(itemView.imageViewMediaPhoto)
            }
        }
    }
}