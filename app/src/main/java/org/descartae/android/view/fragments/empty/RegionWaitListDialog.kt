package org.descartae.android.view.fragments.empty

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.dialog_wait_list.*
import org.descartae.android.AddToWaitlistMutation
import org.descartae.android.DescartaeApp
import org.descartae.android.R
import org.descartae.android.networking.apollo.ApolloApiErrorHandler
import org.descartae.android.networking.apollo.errors.DuplicatedEmailError
import org.descartae.android.networking.apollo.errors.GeneralError
import org.descartae.android.presenter.waitlist.WaitListPresenter
import org.descartae.android.view.events.EventHideLoading
import org.descartae.android.view.events.EventShowLoading
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

class RegionWaitListDialog : DialogFragment() {

    @Inject lateinit var presenter: WaitListPresenter

    @Inject lateinit var eventBus: EventBus

    private var mBuilder: AlertDialog.Builder? = null

    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()

    override fun onStart() {
        super.onStart()
        eventBus.register(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        action_cancel.setOnClickListener { dismiss() }
        action_ok.setOnClickListener { dismiss() }
        action_send.setOnClickListener {

            val email = email.text.toString()

            if (email.isEmpty() || !email.contains("@")) {
                ApolloApiErrorHandler(eventBus).throwError(getString(R.string.wait_list_no_email_error))
            } else {
                ApolloApiErrorHandler.genericErrorMessage = getString(R.string.wait_list_error)

                presenter.setLatLng(longitude, latitude)
                presenter.addToWaitList(email)
            }
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        (activity!!.applicationContext as DescartaeApp).component.inject(this)

        val viewInflated = activity!!.layoutInflater.inflate(R.layout.dialog_wait_list, null) as RelativeLayout

        mBuilder = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.Theme_AppCompat_Light))
        mBuilder!!.setView(viewInflated)

        arguments?.let {
            latitude = it.getDouble(ARG_LATITUDE)
            longitude = it.getDouble(ARG_LONGITUDE)
        }

        return mBuilder!!.create()
    }

    override fun onStop() {
        super.onStop()
        eventBus.unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onError(error: GeneralError) {
        dismiss()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDuplicatedEmailError(duplicatedEmailError: DuplicatedEmailError) {
        dismiss()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAddWaitList(data: AddToWaitlistMutation.Data) {
        if (data.addWaitingUser()!!.success())
            onSuccess()
        else
            dismiss()
    }

    private fun onSuccess() {
        action_cancel.visibility = View.GONE
        action_send.visibility = View.GONE
        action_ok.visibility = View.VISIBLE

        email.visibility = View.GONE

        title.setText(R.string.wait_list_title_success)
        subtitle.setText(R.string.wait_list_desc_success)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventShowLoading(event: EventShowLoading) {
        linear_form.visibility = View.GONE
        loading!!.visibility = View.VISIBLE
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventHideLoading(event: EventHideLoading) {
        loading!!.visibility = View.GONE
        linear_form.visibility = View.VISIBLE
    }

    companion object {

        private const val ARG_LATITUDE = "ARG_LATITUDE"
        private const val ARG_LONGITUDE = "ARG_LONGITUDE"

        fun newInstance(latitude: Double, longitude: Double): RegionWaitListDialog {
            val frag = RegionWaitListDialog()
            val args = Bundle()
            args.putDouble(ARG_LATITUDE, latitude)
            args.putDouble(ARG_LONGITUDE, longitude)
            frag.arguments = args
            return frag
        }
    }

}