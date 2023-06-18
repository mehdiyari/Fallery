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
import ir.mehdiyari.falleryExample.databinding.MediaItemViewBinding

class MediaAdapter : ListAdapter<Pair<String, String>, MediaAdapter.MediaViewHolder>(object :
    DiffUtil.ItemCallback<Pair<String, String>>() {
    override fun areItemsTheSame(
        oldItem: Pair<String, String>,
        newItem: Pair<String, String>
    ): Boolean = oldItem.hashCode() == newItem.hashCode()

    override fun areContentsTheSame(
        oldItem: Pair<String, String>,
        newItem: Pair<String, String>
    ): Boolean = oldItem == newItem
}) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder =
        MediaViewHolder(
            MediaItemViewBinding.bind(
                LayoutInflater.from(parent.context).inflate(R.layout.media_item_view, parent, false)
            )
        )

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind()
    }

    inner class MediaViewHolder(
        private val binding: MediaItemViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            getItem(adapterPosition).apply {
                if (this.second.isEmpty()) {
                    binding.captionContainer.visibility = View.GONE
                } else {
                    binding.captionContainer.visibility = View.VISIBLE
                    binding.appCompatTextViewCaption.text = this.second
                    binding.appCompatTextViewCaption.isSelected = true
                }

                Glide.with(binding.imageViewMediaPhoto)
                    .load(this.first)
                    .placeholder(
                        ColorDrawable(
                            ContextCompat.getColor(
                                itemView.context,
                                R.color.blue_600
                            )
                        )
                    )
                    .centerCrop()
                    .into(binding.imageViewMediaPhoto)
            }
        }
    }
}