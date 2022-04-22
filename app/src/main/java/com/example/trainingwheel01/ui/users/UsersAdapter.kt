package com.example.trainingwheel01.ui.users

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.trainingwheel01.data.entity.UserData
import com.example.trainingwheel01.databinding.UserItemBinding
import jp.wasabeef.glide.transformations.CropCircleTransformation
import timber.log.Timber
import java.lang.IllegalStateException

class UsersAdapter(
    private val onItemClick: (Int, UserData) -> Unit
) : PagingDataAdapter<UserData, RecyclerView.ViewHolder>(USER_COMPARATOR) {

    companion object {
        private val USER_COMPARATOR = object : DiffUtil.ItemCallback<UserData>() {
            override fun areItemsTheSame(oldItem: UserData, newItem: UserData): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areContentsTheSame(oldItem: UserData, newItem: UserData): Boolean {
                return oldItem.email == newItem.email
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is UserViewHolder) {
            holder.bind(getItem(position))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UserViewHolder.create(parent, onItemClick)
    }
}

class UserViewHolder(
    private val binding: UserItemBinding,
    private val onItemClick: (Int, UserData) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(userData: UserData?) {
        if (userData == null) {
            binding.text.text = "Loading.."
        } else {
            binding.text.text = userData.name
            binding.address.text = userData.streetName
            binding.country.text = "${userData.country}, ${userData.nat}"
            Glide.with(binding.imageView)
                .load(userData.photoThumbnail)
                .circleCrop()
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        val exception = e ?: IllegalStateException("Failed to load image")
                        Timber.e(exception)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Timber.d("Drawable: W=${resource?.intrinsicWidth},H=${resource?.intrinsicHeight}")
                        target?.getSize { width, height ->
                            Timber.d("Drawable: TARGET W=${width},H=${height}")
                        }
                        return false
                    }
                })
                .into(binding.imageView)

            itemView.setOnClickListener { onItemClick(bindingAdapterPosition, userData) }
        }
    }

    companion object {
        fun create(parent: ViewGroup, onItemClick: (Int, UserData) -> Unit): UserViewHolder {
            val binding = UserItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return UserViewHolder(binding, onItemClick)
        }
    }
}

