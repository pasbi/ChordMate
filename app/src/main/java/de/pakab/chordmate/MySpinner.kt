package de.pakab.chordmate

import android.content.Context
import android.util.AttributeSet

class MySpinner(
    context: Context,
    attrs: AttributeSet,
) : androidx.appcompat.widget.AppCompatSpinner(context, attrs) {
    interface OnItemSelectedEvenIfSame {
        fun onItemSelected(position: Int)
    }

    var onItemSelected: OnItemSelectedEvenIfSame? = null

    override fun setSelection(position: Int) {
        super.setSelection(position)
        onItemSelected?.onItemSelected(position)
    }
}
