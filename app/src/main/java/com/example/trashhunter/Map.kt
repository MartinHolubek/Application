package com.example.trashhunter

import android.view.MotionEvent
import android.view.View
import androidx.core.widget.NestedScrollView
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView

class Map {
    companion object {
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
                    //super.onTouch(view, event)
                    return true
                }
            }

            return true
        }
    }
}