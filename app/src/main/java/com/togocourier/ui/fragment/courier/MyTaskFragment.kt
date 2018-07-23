package com.togocourier.ui.fragment.courier

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.togocourier.R
import kotlinx.android.synthetic.main.fragment_my_task.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MyTaskFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MyTaskFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyTaskFragment : Fragment(), View.OnClickListener {


    lateinit var tabRightIcon: ImageView
    lateinit var coordinateLay: CoordinatorLayout
    private var fm: FragmentManager? = null
    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var clickedId = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_my_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyTaskFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): MyTaskFragment {
            val fragment = MyTaskFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    private fun initializeView() {
        tabRightIcon = activity?.findViewById<View>(R.id.tabRightIcon) as ImageView
        coordinateLay = activity?.findViewById<View>(R.id.coordinateLay) as CoordinatorLayout
        completeBtn.setOnClickListener(this@MyTaskFragment)
        pendingBtn.setOnClickListener(this@MyTaskFragment)
        tabRightIcon.visibility = View.GONE

        clickedId = R.id.pendingBtn
        replaceFragment(PendingTaskFragment(), false, R.id.childFragmentPlace)
        //replaceFragment(MyPostNewFragment(), false, R.id.childFragmentPlace)
        pendingBtn.setBackgroundResource(R.drawable.blue_left_round)
        pendingBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorWhite))
        completeBtn.setBackgroundResource(R.drawable.white_right_round)
        completeBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimaryDark))
    }

    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean, containerId: Int) {
        val backStackName = fragment.javaClass.name
        val fragmentPopped = childFragmentManager.popBackStackImmediate(backStackName, 0)
        var i = fm?.getBackStackEntryCount()
        if (i != null) {
            while (i > 0) {
                fm?.popBackStackImmediate()
                i--
            }
        }
        if (!fragmentPopped) {
            val transaction = childFragmentManager.beginTransaction()
            //       transaction.setCustomAnimations(R.anim.slide_right_out, R.anim.slide_right_in)
            transaction.replace(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            if (addToBackStack)
                transaction.addToBackStack(backStackName)
            transaction.commit()
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.pendingBtn -> {
                if (clickedId != R.id.pendingBtn) {
                    clickedId = R.id.pendingBtn
                    replaceFragment(PendingTaskFragment(), false, R.id.childFragmentPlace)
                    pendingBtn.setBackgroundResource(R.drawable.blue_left_round)
                    pendingBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorWhite))
                    completeBtn.setBackgroundResource(R.drawable.white_right_round)
                    completeBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimaryDark))
                }
            }
            R.id.completeBtn -> {
                if (clickedId != R.id.completeBtn) {
                    clickedId = R.id.completeBtn
                    replaceFragment(CompleteTaskFragment(), false, R.id.childFragmentPlace)
                    //replaceFragment(MyPostNewFragment(), false, R.id.childFragmentPlace)
                    completeBtn.setBackgroundResource(R.drawable.blue_right_round)
                    completeBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorWhite))
                    pendingBtn.setBackgroundResource(R.drawable.white_left_round)
                    pendingBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimaryDark))
                }
            }

        }
    }
}// Required empty public constructor
