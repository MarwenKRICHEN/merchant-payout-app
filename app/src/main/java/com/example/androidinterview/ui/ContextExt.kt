package com.example.androidinterview.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/** The Activity this context belongs to, unwrapping [ContextWrapper]s. */
tailrec fun Context.findActivity(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> error("No Activity in this context")
}
