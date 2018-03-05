package org.descartae.android.view.fragments.facility

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.dialog_feedback.*
import org.descartae.android.AddFeedbackMutation
import org.descartae.android.DescartaeApp
import org.descartae.android.R
import org.descartae.android.networking.apollo.errors.GeneralError
import org.descartae.android.presenter.feedback.FeedbackPresenter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

class FeedbackDialog : DialogFragment() {

    @Inject lateinit var presenter: FeedbackPresenter

    @Inject lateinit var eventBus: EventBus

    private var facilityID: String? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity!!.applicationContext as DescartaeApp).component.inject(this)

        presenter.setFacilityId(facilityID)

        button_cancel.setOnClickListener { dismiss() }
        button_ok.setOnClickListener { dismiss() }
        button_send.setOnClickListener {

            val feedback: String = editText_message.text.toString()

            if (feedback.isEmpty())
                Snackbar.make(editText_message, R.string.feedback_no_message_error, Snackbar.LENGTH_SHORT).show()
            else presenter.sendFeedback(feedback)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val viewInflated = activity!!.layoutInflater.inflate(R.layout.dialog_feedback, null) as LinearLayout

        facilityID = arguments?.getString("facilityID")

        if (facilityID == null) {
            textView_title.setText(R.string.feedback_title)
            textView_subtitle.setText(R.string.feedback_desc)
        } else {
            textView_title.setText(R.string.feedback_facility_title)
            textView_subtitle.setText(R.string.feedback_facility_desc)
        }

        val mBuilder = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.Theme_AppCompat_Light))
        mBuilder.setView(viewInflated)

        return mBuilder.create()
    }

    override fun onStart() {
        super.onStart()
        eventBus.register(this)
    }

    override fun onStop() {
        super.onStop()
        eventBus.unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFeedbackResult(data: AddFeedbackMutation.Data) {
        if (data.addFeedback().success()) {
            button_cancel.visibility = View.GONE
            button_send.visibility = View.GONE
            button_ok.visibility = View.VISIBLE

            editText_message.visibility = View.GONE

            if (facilityID == null) {
                textView_title.setText(R.string.feedback_title_success)
                textView_subtitle.setText(R.string.feedback_desc_success)
            } else {
                textView_title.setText(R.string.feedback_facility_title_success)
                textView_subtitle.setText(R.string.feedback_facility_desc_success)
            }
        } else {
            Snackbar.make(editText_message, R.string.feedback_error, Snackbar.LENGTH_SHORT).show()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onError(error: GeneralError) {
        dismiss()
    }

    companion object {

        fun newInstance(facilityID: String?): FeedbackDialog {
            val frag = FeedbackDialog()
            val args = Bundle()
            args.putString("facilityID", facilityID)
            frag.arguments = args
            return frag
        }
    }
}
