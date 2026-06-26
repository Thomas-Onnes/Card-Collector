package com.example.cardcollector.ui

import android.text.method.TransformationMethod
import android.view.View

class FixedPasswordTransformationMethod : TransformationMethod {

    override fun getTransformation(
        source: CharSequence,
        view: View
    ): CharSequence {
        return PasswordCharSequence(source)
    }

    override fun onFocusChanged(
        view: View,
        sourceText: CharSequence,
        focused: Boolean,
        direction: Int,
        previouslyFocusedRect: android.graphics.Rect?
    ) {
        // No focus behavior needed.
    }

    private class PasswordCharSequence(
        private val source: CharSequence
    ) : CharSequence {

        override val length: Int
            get() = source.length

        override fun get(index: Int): Char {
            return '•'
        }

        override fun subSequence(
            startIndex: Int,
            endIndex: Int
        ): CharSequence {
            return "•".repeat(endIndex - startIndex)
        }

        override fun toString(): String {
            return "•".repeat(source.length)
        }
    }
}