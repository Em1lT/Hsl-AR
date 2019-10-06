package com.example.hslar.PopUp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.example.hslar.Observer.OnButtonClick
import com.example.hslar.R
import kotlinx.android.synthetic.main.notification_pop_up.*
import kotlinx.android.synthetic.main.notification_pop_up.view.*


@SuppressLint("ValidFragment")
class NotificationPopUp(val dist: Int) : DialogFragment(){

    private var notificationDistance = 100
    var listener: OnButtonClick? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        return inflater.inflate(R.layout.notification_pop_up, container, false)
        }

    @SuppressLint("LogNotTimber")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listener = context as OnButtonClick

        view.distanceFromYou.text = "$dist m"
        view.seekBar.max = (dist - 1000)

        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {

            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
              view.distanceFromYou1.text = "$p1 m"
                view.distanceFromBus.text = "${dist - p1} m"
                notificationDistance = p1

            }
            override fun onStartTrackingTouch(p0: SeekBar?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
        view.bStartNotification.setOnClickListener {
            listener!!.onDialogClickListener(view.toggleButton.isChecked, notificationDistance)
            dialog.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
    }
    override fun onPause() {
        super.onPause()
    }
}