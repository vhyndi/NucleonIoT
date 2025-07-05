package com.nucleon.iot.ui.main.device

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nucleon.iot.R
import com.nucleon.iot.data.model.Relay
import com.nucleon.iot.databinding.ItemRelayBinding

class RelayAdapter(
    private val onRelayClick: (Relay, Int) -> Unit
) : ListAdapter<Relay, RelayAdapter.RelayViewHolder>(RelayDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelayViewHolder {
        val binding = ItemRelayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RelayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RelayViewHolder, position: Int) {
        holder.bind(getItem(position), position, onRelayClick)
    }

    class RelayViewHolder(
        private val binding: ItemRelayBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(relay: Relay, position: Int, onRelayClick: (Relay, Int) -> Unit) {
            binding.apply {
                // Set relay name
                relayNameText.text = relay.name

                // Set status and appearance
                val isOn = relay.status == "on"
                relayStatusText.text = if (isOn) "ON" else "OFF"

                // Update background and icon colors with animation
                if (isOn) {
                    relayBackground.setBackgroundResource(R.drawable.circle_relay_on)
                    relayIcon.setColorFilter(
                        root.context.getColor(R.color.white),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                } else {
                    relayBackground.setBackgroundResource(R.drawable.circle_relay_off)
                    relayIcon.setColorFilter(
                        root.context.getColor(R.color.white),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }

                // Click listener with animation
                root.setOnClickListener {
                    // Scale animation
                    val scaleX = ObjectAnimator.ofFloat(relayBackground, "scaleX", 1f, 0.9f, 1f)
                    val scaleY = ObjectAnimator.ofFloat(relayBackground, "scaleY", 1f, 0.9f, 1f)

                    val animatorSet = AnimatorSet()
                    animatorSet.playTogether(scaleX, scaleY)
                    animatorSet.duration = 200
                    animatorSet.start()

                    onRelayClick(relay, position)
                }
            }
        }
    }

    class RelayDiffCallback : DiffUtil.ItemCallback<Relay>() {
        override fun areItemsTheSame(oldItem: Relay, newItem: Relay): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Relay, newItem: Relay): Boolean {
            return oldItem == newItem
        }
    }
}