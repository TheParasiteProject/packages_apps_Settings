/*
 * SPDX-FileCopyrightText: The dotOS Project
 * SPDX-FileCopyrightText: TheParasiteProject
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.deviceinfo.aboutphone

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.android.settings.R
import com.google.android.material.card.MaterialCardView

@SuppressLint("ClickableViewAccessibility")
open class AboutBaseCard : MaterialCardView {
    protected var layout: RelativeLayout
    protected var defaultPadding = 38
    var defaultRadius = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) : super(context, attrs, defStyleAttr)

    init {
        defaultRadius = resources.getDimensionPixelSize(R.dimen.contextual_card_corner_radius)
        layoutParams =
            LayoutParams(
                resources.getDimensionPixelSize(R.dimen.storage_card_min_width),
                resources.getDimensionPixelSize(R.dimen.storage_card_min_height),
            )
        layout = RelativeLayout(context)
        layout.layoutParams =
            RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT,
            )
        layout.setPadding(
            defaultPadding,
            (defaultPadding * 1.5).toInt(),
            defaultPadding,
            (defaultPadding * 1.5).toInt(),
        )
        layout.setBackgroundColor(
            resources.getColor(R.color.contextual_card_background_color, context.theme)
        )
        addView(layout)
        radius = defaultRadius.toFloat()
        setCardBackgroundColor(
            resources.getColor(R.color.contextual_card_background_color, context.theme)
        )
        cardElevation = 0f
        strokeColor = resources.getColor(R.color.contextual_card_stroke_color, context.theme)
        strokeWidth = resources.getDimensionPixelSize(R.dimen.contextual_card_stroke_width)

        isClickable = true
        foreground =
            context
                .obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
                .use { it.getDrawable(0) }
    }
}
