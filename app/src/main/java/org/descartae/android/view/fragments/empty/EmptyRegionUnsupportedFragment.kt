package org.descartae.android.view.fragments.empty

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.region_unsupported_empty.action_notify_me
import org.descartae.android.R

class EmptyRegionUnsupportedFragment : Fragment() {

  private var latitude: Double = 0.toDouble()
  private var longitude: Double = 0.toDouble()
  private var mListener: Listener? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (arguments != null) {
      latitude = arguments!!.getDouble(ARG_LAT)
      longitude = arguments!!.getDouble(ARG_LNG)
    }
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    action_notify_me.setOnClickListener { mListener?.showWaitListDialog(latitude, longitude) }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.region_unsupported_empty, container, false)
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    if (context is Listener) {
      mListener = context
    } else {
      throw RuntimeException(context!!.toString() + " must implement Listener")
    }
  }

  override fun onDetach() {
    super.onDetach()
    mListener = null
  }

  interface Listener {
    fun showWaitListDialog(latitude: Double, longitude: Double)
  }

  companion object {

    private const val ARG_LAT = "ARG_LAT"
    private const val ARG_LNG = "ARG_LNG"

    fun newInstance(latitude: Double, longitude: Double): EmptyRegionUnsupportedFragment {
      val fragment = EmptyRegionUnsupportedFragment()
      val args = Bundle()
      args.putDouble(ARG_LAT, latitude)
      args.putDouble(ARG_LNG, longitude)
      fragment.arguments = args
      return fragment
    }
  }
}
