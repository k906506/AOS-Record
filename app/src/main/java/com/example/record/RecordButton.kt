package com.example.record

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton

class RecordButton(
    context: Context,
    attrs: AttributeSet
) : AppCompatImageButton(context, attrs) {

    init {
        setBackgroundResource(R.drawable.shape_oval_button)
    }

    fun updateIconWithState(state: State) {
        when (state) {
            State.BEFORE_RECORDING ->
                setImageResource(R.drawable.ic_record)
            State.ON_RECORDING ->
                setImageResource(R.drawable.ic_stop)
            State.AFTER_RECORDING ->
                setImageResource(R.drawable.ic_play)
            State.ON_PALYING ->
                setImageResource(R.drawable.ic_stop)
        }
    }
}