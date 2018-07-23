package com.togocourier.ui.activity.customer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.togocourier.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.togocourier.responceBean.GetLatLngInfo
import com.togocourier.util.Constant
import com.togocourier.util.PreferenceConnector
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.custommarkerlayout.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class MapActivity : FragmentActivity(), OnMapReadyCallback {


    private var mMap: GoogleMap? = null
    private val latlngs=ArrayList<LatLng>()
    var getLatLngInfo = GetLatLngInfo().result
    var postId = ""
    var applyUserId = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        postId =  intent.getStringExtra("postId")
        applyUserId =  intent.getStringExtra("applyUserId")
        getLatLongById (postId,applyUserId)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        back.setOnClickListener {
            onBackPressed()
        }

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        // Add a marker in Sydney, Australia, and move the camera.
    }


    private fun getLatLongById(postId: String, applyUserId: String) {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            popProgressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.getLatLongById,
                    Response.Listener { response ->
                        popProgressBar.visibility = View.GONE
                        println("#" + response)
                        Log.e("GETPOSTDATA", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                val gson = Gson()
                                var getLatLngInfo = gson.fromJson(response, GetLatLngInfo::class.java)

                                var myLatitude:Double = getLatLngInfo?.result?.latitude?.toDouble()!!
                                var myLongitude:Double = getLatLngInfo?.result?.longitude?.toDouble()!!

                                var deliverLat:Double = getLatLngInfo?.result?.deliverLat?.toDouble()!!
                                var deliverLong:Double = getLatLngInfo?.result?.deliverLong?.toDouble()!!
                                var pickupLat:Double = getLatLngInfo?.result?.pickupLat?.toDouble()!!
                                var pickupLong:Double = getLatLngInfo?.result?.pickupLong?.toDouble()!!

                                var pickupAdrs:String = getLatLngInfo?.result?.pickupAdrs.toString()
                                var deliveryAdrs:String = getLatLngInfo?.result?.deliveryAdrs.toString()

                                val currentLocation =  LatLng(myLatitude, myLongitude)
                                val pickUpMarker = LatLng(deliverLat, deliverLong)
                                val dropMarker = LatLng(pickupLat, pickupLong)


                                val markerView = (this.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.custommarkerlayout, null)
                                val displayMetrics = DisplayMetrics()
                                markerView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                                markerView.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
                                markerView.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
                                markerView.buildDrawingCache()

                                if(!getLatLngInfo.result!!.profileImage?.equals("")!!){
                                    Picasso.with(this)
                                            .load(getLatLngInfo.result!!.profileImage).placeholder(R.drawable.splash_logo)
                                            .into(markerView.marker_image, object : com.squareup.picasso.Callback {
                                                override fun onSuccess() {

                                                    val finalBitmap = Bitmap.createBitmap(markerView.measuredWidth, markerView.measuredHeight, Bitmap.Config.ARGB_8888)
                                                    val canvas = Canvas(finalBitmap)
                                                    markerView.draw(canvas)

                                                    // update views
                                                    val point: LatLng
                                                    val newLat = java.lang.Double.parseDouble(myLatitude.toString()) + (Math.random() - .5) / 1500// * (Math.random() * (max - min) + min);
                                                    val newLng = java.lang.Double.parseDouble(myLongitude.toString()) + (Math.random() - .5) / 1500// * (Math.random() * (max - min) + min);
                                                    point = LatLng(newLat, newLng)
                                                    // point = new LatLng(Double.parseDouble(mapBean.latitude), Double.parseDouble(mapBean.longitude));
                                                    // Creating an instance of MarkerOptions
                                                    val markerOptions = MarkerOptions()
                                                    markerOptions.position(point)

                                                    markerOptions.snippet("meri location")
                                                    mMap?.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromBitmap(finalBitmap)))
                                                }

                                                override fun onError() {
                                                    Log.d("ERROR::", "In error")
                                                }
                                            })
                                }




                                //mMap?.addMarker(MarkerOptions().position(currentLocation).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.current_map)));
                                mMap?.addMarker(MarkerOptions().position(pickUpMarker).title(pickupAdrs).icon(BitmapDescriptorFactory.fromResource(R.drawable.sorce_map)));
                                mMap?.addMarker(MarkerOptions().position(dropMarker).title(deliveryAdrs).icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_map)));

                                mMap?.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))

                                mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 10f))
                                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    //return
                                }
                                mMap?.setMyLocationEnabled(true)
                                mMap?.getUiSettings()?.setAllGesturesEnabled(true)
                                mMap?.getUiSettings()?.setMyLocationButtonEnabled(true)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (ContextCompat.checkSelfPermission(this,
                                                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        mMap?.setMyLocationEnabled(true)
                                    }
                                } else {
                                    mMap?.setMyLocationEnabled(true)
                                }

                            } else {
                                Constant.snackbar(mainLayout, message)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        popProgressBar.visibility = View.GONE
                        Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@MapActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("postId", postId)
                    params.put("applyUserId", applyUserId)
                    return params
                }
            }
            stringRequest.setRetryPolicy(DefaultRetryPolicy(
                    20000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
            VolleySingleton.getInstance(baseContext).addToRequestQueue(stringRequest)
        }
    }
}
