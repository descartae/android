package org.descartae.android.view.fragments.empty

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_offline_empty.action_try
import org.descartae.android.R
import org.descartae.android.interfaces.RetryConnectionView

class EmptyOfflineFragment : Fragment() {

  private var mListener: RetryConnectionView? = null

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    action_try.setOnClickListener { mListener?.onRetryConnection() }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_offline_empty, container, false)
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    if (context is RetryConnectionView) {
      mListener = context
    } else {
      throw RuntimeException(context!!.toString() + " must implement RetryConnectionView")
    }
  }

  override fun onDetach() {
    super.onDetach()
    mListener = null
  }

  companion object {

    fun newInstance(): EmptyOfflineFragment {
      val fragment = EmptyOfflineFragment()
      val args = Bundle()
      fragment.arguments = args
      return fragment
    }
  }
}