package com.togocourier.ui.activity

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.togocourier.R
import kotlinx.android.synthetic.main.activity_welcome_courier.*

class welcomeActivityCourier : AppCompatActivity() {
    private var dots = ArrayList<TextView>()
    private var myViewPagerAdapter: MyViewPagerAdapter? = null

    val layouts = intArrayOf(
            R.layout.one_cour_side_layout,
            R.layout.two_cour_side_layout,
            R.layout.three_cour_side_layout,
            R.layout.four_cour_side_layout,
            R.layout.five_cour_side_layout,
            R.layout.six_cour_side_layout,
            R.layout.seven_cour_side_layout)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_courier)

        addBottomDots(0)

        var viewPagerPageChangeListener : ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {

            override fun onPageSelected(position: Int) {
                addBottomDots(position)
                // changing the next button text 'NEXT' / 'GOT IT'
                if (position == layouts.size - 1) {
                    // last page. make button text to GOT IT
                    btn_next.setText(getString(R.string.start))
                    btn_skip.setVisibility(View.GONE)
                } else {
                    // still pages are left
                    btn_next.setText(getString(R.string.next))
                    btn_skip.setVisibility(View.VISIBLE)
                }
            }

            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {

            }

            override fun onPageScrollStateChanged(arg0: Int) {

            }
        }

        myViewPagerAdapter = MyViewPagerAdapter(this)
        view_pager.setAdapter(myViewPagerAdapter)
        view_pager.addOnPageChangeListener(viewPagerPageChangeListener)

        fun getItem(i: Int): Int {
            return view_pager.getCurrentItem() + i
        }

        btn_skip.setOnClickListener {
            launchHomeScreen()
        }

        btn_next.setOnClickListener {
            val current = getItem(+1)
            if (current < layouts.size) {
                // move to next screen
                view_pager.setCurrentItem(current)
            } else {
                launchHomeScreen()
            }
        }

    }


    private fun launchHomeScreen() {
        startActivity(Intent(this@welcomeActivityCourier, SignInActivity::class.java))
        finish()
    }


    fun addBottomDots(currentPage: Int) {
        // dots = arrayOfNulls<TextView>(layouts.size) as  ArrayList<TextView>
        dots = ArrayList<TextView>(layouts.size)


        val colorsActive = resources.getIntArray(R.array.array_dot_active)
        val colorsInactive = resources.getIntArray(R.array.array_dot_inactive)

        layoutDots.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i].text = Html.fromHtml("&#8226;")
            dots[i].textSize = 35f
            dots[i].setTextColor(colorsInactive[currentPage])

            layoutDots.addView(dots[i])
        }

        if (dots.size > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage])
    }

    inner class MyViewPagerAdapter(var mContext: Context) : PagerAdapter() {

        private var layoutInflater: LayoutInflater? = null

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return layouts.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {

            layoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?

            val view = layoutInflater?.inflate(layouts[position], container, false)
            container.addView(view)
            return view!!
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = `object` as View
            container.removeView(view)
        }
    }
}
