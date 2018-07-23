package com.togocourier.ui.fragment.customer

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.togocourier.R
import com.togocourier.ui.activity.customer.AddPostActivity
import kotlinx.android.synthetic.main.fragment_my_post.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MyPostFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MyPostFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyPostFragment : Fragment(), View.OnClickListener {
    lateinit var tabRightIcon: ImageView
    lateinit var coordinateLay: CoordinatorLayout
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var fm: FragmentManager? = null
    private var clickedId = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view  = inflater!!.inflate(R.layout.fragment_my_post, container, false)
        return view
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
        var isAddPost = false

        fun newInstance(param1: String, param2: String): MyPostFragment {
            val fragment = MyPostFragment()
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
        tabRightIcon.setOnClickListener(this@MyPostFragment)
        pendingBtn.setOnClickListener(this)
        newBtn.setOnClickListener(this)
        completeBtn.setOnClickListener(this)

        tabRightIcon.visibility = View.VISIBLE

        clickedId = R.id.newBtn
        replaceFragment(MyPostNewFragment(), false, R.id.childFragmentPlace)
        //replaceFragment(MyPostNewFragment(), false, R.id.childFragmentPlace)
        newBtn.setBackgroundResource(R.drawable.blue_right_round)
        newBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorWhite))
        pendingBtn.setBackgroundResource(R.drawable.white_left_round)
        pendingBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimaryDark))
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.tabRightIcon -> {
                activity?.startActivity(Intent(activity, AddPostActivity::class.java))
            }
            R.id.pendingBtn -> {
                if(MyPostPendingFragment() != null) {
                    replaceFragment(MyPostPendingFragment(), false, R.id.childFragmentPlace)
                    pendingBtn.setBackgroundResource(R.drawable.blue_left_round)
                    pendingBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorWhite))
                    newBtn.setBackgroundResource(R.drawable.white_right_round)
                    newBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimaryDark))

                    completeBtn.setBackgroundResource(R.drawable.white_rectangle_shap)
                    completeBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimaryDark))

                    pendingBtn.isEnabled = false
                    completeBtn.isEnabled = true
                    newBtn.isEnabled = true
                }

            }
            R.id.completeBtn -> {
                if(CompletedPostFragment() != null) {
                    replaceFragment(CompletedPostFragment(), false, R.id.childFragmentPlace)
                    newBtn.setBackgroundResource(R.drawable.white_right_round)
                    newBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimaryDark))
                    pendingBtn.setBackgroundResource(R.drawable.white_left_round)
                    pendingBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimaryDark))

                    completeBtn.setBackgroundResource(R.drawable.primary_rectangle_shap)
                    completeBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorWhite))

                    pendingBtn.isEnabled = true
                    completeBtn.isEnabled = false
                    newBtn.isEnabled = true
                }
            }
            R.id.newBtn -> {
                if(MyPostNewFragment() != null) {

                    replaceFragment(MyPostNewFragment(), false, R.id.childFragmentPlace)
                    //replaceFragment(MyPostNewFragment(), false, R.id.childFragmentPlace)
                    newBtn.setBackgroundResource(R.drawable.blue_right_round)
                    newBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorWhite))
                    pendingBtn.setBackgroundResource(R.drawable.white_left_round)
                    pendingBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimaryDark))

                    completeBtn.setBackgroundResource(R.drawable.white_rectangle_shap)
                    completeBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimaryDark))

                    pendingBtn.isEnabled = true
                    completeBtn.isEnabled = true
                    newBtn.isEnabled = false
                }
            }

        }
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
            transaction.replace(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_UNSET)
            if (addToBackStack)
                transaction.addToBackStack(backStackName)
            transaction.commit()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("TAG", "OnResumeCall")
       /* if (MyPostFragment.isAddPost) {
            clickedId = R.id.newBtn
            replaceFragment(MyPostNewFragment(), false, R.id.childFragmentPlace)
            //replaceFragment(MyPostNewFragment(), false, R.id.childFragmentPlace)
            newBtn.setBackgroundResource(R.drawable.blue_right_round)
            newBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorWhite))
            pendingBtn.setBackgroundResource(R.drawable.white_left_round)
            pendingBtn.setTextColor(ContextCompat.getColor(context!!, R.color.colorPrimaryDark))
        }*/
    }
}// Required empty public constructor
