package com.holubek.trashhunter

import android.view.MotionEvent
import android.view.View
import androidx.core.widget.NestedScrollView
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView

/**
 * Trieda na prácu s mapou
 */
class Map {
    companion object {

        /**
         * Nastaví pohyb mapovej vrstvy v ScrollView
         * @param root reprezentuje objekt View obrazovky v ktorej sa nachádza MapView
         * @param mapView objekt View zobrazujúci mapu
         * @param scrollView objekt View v ktorom je MapView
         */
        @JvmStatic
        fun setMove(root: View, mapView: MapView, scrollView: NestedScrollView): Boolean {
            mapView.onTouchListener = object: DefaultMapViewOnTouchListener(root.context, mapView) {

                override fun onTouch(view: View?, event: MotionEvent?): Boolean {

                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            scrollView.requestDisallowInterceptTouchEvent(true)
                        }
                        MotionEvent.ACTION_UP -> {
                            scrollView.requestDisallowInterceptTouchEvent(true)
                        }
                    }
                    super.onTouch(view, event)
                    return true
                }
            }

            return true
        }
    }
}