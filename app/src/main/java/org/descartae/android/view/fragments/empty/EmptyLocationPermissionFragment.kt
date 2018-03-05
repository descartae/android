package org.descartae.android.view.fragments.empty

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_permission_empty.*
import org.descartae.android.R
import org.descartae.android.interfaces.RequestPermissionView


class EmptyLocationPermissionFragment : Fragment() {

    private var mListener: RequestPermissionView? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        action_allow.setOnClickListener { mListener?.onAcceptPermission() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_permission_empty, container, false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is RequestPermissionView) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement RequestPermissionView")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    companion object {

        fun newInstance(): EmptyLocationPermissionFragment {
            val fragment = EmptyLocationPermissionFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
