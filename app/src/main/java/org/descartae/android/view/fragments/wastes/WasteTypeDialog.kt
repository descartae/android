package org.descartae.android.view.fragments.wastes

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_waste_type_dialog.*
import org.descartae.android.R

class WasteTypeDialog : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_waste_type_dialog, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        close.setOnClickListener { dismiss() }

        arguments?.let {
            name.text = it.getString("name")
            desc.text = it.getString("desc")
            Picasso.with(activity).load(it.getString("icon")).placeholder(R.drawable.ic_placeholder).into(icon)
        }
    }

    companion object {

        fun newInstance(name: String, desc: String, icon: String): WasteTypeDialog {
            val frag = WasteTypeDialog()
            val args = Bundle()
            args.putString("name", name)
            args.putString("desc", desc)
            args.putString("icon", icon)
            frag.arguments = args
            return frag
        }
    }
}