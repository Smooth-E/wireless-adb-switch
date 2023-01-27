package com.smoothie.wirelessDebuggingSwitch.widget

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.widget.ImageView
import android.widget.RemoteViews
import com.smoothie.wirelessDebuggingSwitch.R
import com.smoothie.wirelessDebuggingSwitch.Utilities

object RoundedWidgetUtilities {

    fun applyRemoteViewsParameters(
        context: Context,
        preferences: SharedPreferences,
        remoteViews: RemoteViews,
        cornerViewIds: Array<Int> = arrayOf(
            R.id.corner_bottom_left,
            R.id.corner_bottom_right,
            R.id.corner_top_left,
            R.id.corner_top_right
        ),
        plainViewIds: Array<Int> = arrayOf(
            R.id.side_top,
            R.id.side_bottom,
            R.id.side_left,
            R.id.side_right,
            R.id.center
        )
    ) {
        val radius = Utilities.getWidgetCornerRadius(context, preferences)

        val cornerBitmap = Utilities.generateWidgetCornerBitmap(context, preferences, radius)
        cornerViewIds.forEach { id ->
            remoteViews.setBitmap(id, "setImageBitmap", cornerBitmap)
        }

        val centerBitmap = Utilities.generateRectangleBitmapForWidget(context, preferences)
        plainViewIds.forEach { id ->
            remoteViews.setBitmap(id, "setImageBitmap", centerBitmap)
        }
    }

    fun applyPreviewParameters(
        context: Context,
        preferences: SharedPreferences,
        view: View,
        cornerViewIds: Array<Int> = arrayOf(
            R.id.corner_bottom_left,
            R.id.corner_bottom_right,
            R.id.corner_top_left,
            R.id.corner_top_right
        ),
        plainViewIds: Array<Int> = arrayOf(
            R.id.side_top,
            R.id.side_bottom,
            R.id.side_left,
            R.id.side_right,
            R.id.center
        )
    ) {
        val radius =  Utilities.getWidgetCornerRadius(context, preferences)

        val cornerBitmap = Utilities.generateWidgetCornerBitmap(context, preferences, radius)
        cornerViewIds.forEach { id ->
            view.findViewById<ImageView>(id).setImageBitmap(cornerBitmap)
        }

        val centerBitmap = Utilities.generateRectangleBitmapForWidget(context, preferences)
        plainViewIds.forEach { id ->
            view.findViewById<ImageView>(id).setImageBitmap(centerBitmap)
        }
    }

}
