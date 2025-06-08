package de.pakab.chordmate

import android.content.Context
import android.util.AttributeSet

class MySpinner(
    context: Context,
    attrs: AttributeSet,
) : androidx.appcompat.widget.AppCompatSpinner(context, attrs) {
    interface OnItemSelectedEvenIfSameListener {
        fun onItemSelected(position: Int)
    }

    var onItemSelectedListener: OnItemSelectedEvenIfSameListener? = null

    override fun setSelection(position: Int) {
        super.setSelection(position)
        onItemSelectedListener?.onItemSelected(position)
    }
}
