package org.descartae.android.view.fragments.wastes

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_waste_type_dialog.*
import org.descartae.android.R

class WasteTypeDialog : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val stringName = arguments!!.getString("name")
        val stringDesc = arguments!!.getString("desc")
        val stringIcon = arguments!!.getString("icon")

        val v = inflater.inflate(R.layout.fragment_waste_type_dialog, container, false)

        ButterKnife.bind(this, v)

        name.text = stringName
        desc.text = stringDesc
        Picasso.with(activity).load(stringIcon).placeholder(R.drawable.ic_placeholder).into(icon)

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        close.setOnClickListener { dismiss() }
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