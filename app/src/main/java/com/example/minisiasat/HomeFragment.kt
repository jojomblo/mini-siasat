package com.example.minisiasat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.minisiasat.utils.Users

class HomeFragment : Fragment() {

    companion object {
        private const val ARG_USER = "user"

        fun newInstance(user: Users): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            args.putSerializable(ARG_USER, user)
            fragment.arguments = args
            return fragment
        }
    }

    private var user: Users? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = arguments?.getSerializable(ARG_USER) as? Users
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(android.R.layout.simple_list_item_1, container, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = "Welcome, ${user?.name ?: "User"}\nRole: ${user?.role ?: "-"}"
        return view
    }
}