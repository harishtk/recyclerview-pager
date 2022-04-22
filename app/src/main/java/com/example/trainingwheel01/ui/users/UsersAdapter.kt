package com.example.trainingwheel01.ui.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trainingwheel01.data.entity.UserData
import com.example.trainingwheel01.databinding.UserItemBinding

class UsersAdapter : PagingDataAdapter<UserData, RecyclerView.ViewHolder>(USER_COMPARATOR) {

    companion object {
        private val USER_COMPARATOR = object : DiffUtil.ItemCallback<UserData>() {
            override fun areItemsTheSame(oldItem: UserData, newItem: UserData): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: UserData, newItem: UserData): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is UserViewHolder) {
            holder.bind(getItem(position))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UserViewHolder.create(parent)
    }
}

class UserViewHolder(
    private val binding: UserItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(userData: UserData?) {
        if (userData == null) {
            binding.text.text = "Loading.."
        } else {
            binding.text.text = userData.name
            Glide.with(binding.imageView)
                .load(userData.photoThumbnail)
                .into(binding.imageView)
        }
    }

    companion object {
        fun create(parent: ViewGroup): UserViewHolder {
            val binding = UserItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return UserViewHolder(binding)
        }
    }
}

