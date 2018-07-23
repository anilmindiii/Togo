package com.togocourier.ui.activity.courier

import android.app.AlertDialog
import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.*
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.togocourier.R
import com.togocourier.responceBean.PaymentResponce
import com.togocourier.responceBean.StateBean
import com.togocourier.util.*
import com.togocourier.vollyemultipart.VolleySingleton
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import kotlinx.android.synthetic.main.activity_courier_payment.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

class CourierPaymentActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    var expireMnth = ""
    var expireYear = ""
    var stateName = ""
    var cityName = ""
    var cityList = ArrayList<String>()
    var stateList = ArrayList<String>()
    private var fromDate: DatePickerDialog? = null
    private var now: Calendar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courier_payment)

        getCountrylist()
        spinner!!.setOnItemSelectedListener(this)

        btn_bank_pay.setOnClickListener {
            var first_name =   first_name.text.toString()
            var last_name =   last_name.text.toString()
            var tv_dob =   tv_dob.text.toString()
            var address =   address.text.toString()
            var postal_code =   postal_code.text.toString()
            var ssn_last =   ssn_last.text.toString()
            var routing_num =   routing_num.text.toString()
            var accNum = account_num.text.toString()

            if(isAddBankVaild()){
                cardPay(expireMnth,expireYear,"","","",stateName,
                        address,postal_code,cityName,accNum,routing_num,"","",tv_dob,ssn_last)
            }
        }

        iv_back_press.setOnClickListener {
            onBackPressed()
        }

        tv_dob.setOnClickListener {
            setDateField()
        }


        var adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cityList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.setAdapter(adapter)

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        stateName = stateList.get(p2)
        cityName = cityList.get(p2)
        tv_state.text = stateName
    }

    private fun setDateField() {
        now = Calendar.getInstance()
        fromDate = DatePickerDialog.newInstance({ datePickerDialog, year, monthOfYear, dayOfMonth ->
            var date = year.toString() + "-" + (monthOfYear + 1).toString() + "-" + dayOfMonth
            tv_dob.text = date
            //  dob = date
        }, now!!.get(Calendar.YEAR), now!!.get(Calendar.MONTH), now!!.get(Calendar.DAY_OF_MONTH))
        fromDate?.setMaxDate(Calendar.getInstance())
        fromDate?.setAccentColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        fromDate?.show(this.getFragmentManager(), "")
        fromDate?.setOnCancelListener(DialogInterface.OnCancelListener {
            Log.d("TimePicker", "Dialog was cancelled")
            fromDate?.dismiss()
        })

    }


    fun cardPay(exp_month: String, exp_year: String, cvv: String,
                totalcardNum: String, cardHolderName: String,state:String,
                address:String,postalCode:String,city:String,accountNo:String,routingNumber:String,
                currency:String,country:String,dob:String,ssnLast:String) {

        if (Constant.isNetworkAvailable(this, mainLayout)) {
            progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.CoriorBankAdd,
                    Response.Listener { response ->
                        progressBar.visibility = View.GONE
                        println("#" + response)
                        Log.e("Stripes", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                var gson = Gson()
                                var paymentInfo = gson.fromJson(response, PaymentResponce::class.java)
                                AddCardSuccess(paymentInfo.message.toString())
                            } else {

                                var userType = PreferenceConnector.readString(this,PreferenceConnector.USERTYPE,"")
                                if(userType.equals(Constant.COURIOR)){
                                    if(message.equals("Currently you are inactivate user")){
                                        var helper = HelperClass(this,this)
                                        helper.inActiveByAdmin("Admin inactive your account",true)
                                    }else{
                                        Constant.snackbar(mainLayout, message)
                                    }
                                }else{
                                    Constant.snackbar(mainLayout, message)
                                }

                            }
                        } catch (e: JSONException) {
                            Constant.snackbar(mainLayout, "Something went wrong...")
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {error->
                        progressBar.visibility = View.GONE
                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse?.statusCode == 400){
                                var helper = HelperClass(this,this)
                                helper.sessionExpairDialog()
                            }
                        }
                    })
            {

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@CourierPaymentActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("firstName", first_name.text.toString())
                    params.put("lastName", last_name.text.toString())
                    params.put("dob", dob)
                    params.put("country", "US")
                    params.put("currency", "USD")
                    params.put("routingNumber",routingNumber)
                    params.put("accountNo",accountNo)
                    params.put("address",address)
                    params.put("postalCode",postalCode)
                    params.put("city", city)
                    params.put("state", state)
                    params.put("ssnLast", ssnLast)
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



    private fun getCountrylist() {
        val gson = Gson()
        val stateJsonString = loadJSONFromAsset()
        val stateBean = gson.fromJson<StateBean>(stateJsonString, StateBean::class.java!!)
        Collections.sort(stateBean.getState())
        if (stateBean.state.size > 0) {
            for (i in 0 until stateBean.getState().size) {
                val splitState = stateBean.getState().get(i).split(",")
                cityList.add(splitState[0])
                stateList.add(splitState[1])
            }
        }
    }

    private fun loadJSONFromAsset(): String? {
        var json: String? = null
        try {
            val isAvail = this.getAssets().open("us.json")
            val size = isAvail.available()
            val buffer = ByteArray(size)
            isAvail.read(buffer)
            isAvail.close()
            json  = String(buffer, Charset.forName("UTF-8"))

        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    fun AddCardSuccess(msg:String) {

        val alertDialog = AlertDialog.Builder(this)

        alertDialog.setTitle("Alert")

        alertDialog.setCancelable(false)
        alertDialog.setMessage(msg)

        alertDialog.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
            this.finish()
        })

        alertDialog.show()

    }



    fun isAddBankVaild(): Boolean {
        val v = Validation()

        if (v.isEmpty(first_name)) {
            Constant.snackbar(mainLayout, "First name can't be empty")
            first_name.requestFocus()
            return false
        }
        else if (v.isEmpty(last_name)) {
            Constant.snackbar(mainLayout, "Last name can't be empty")
            last_name.requestFocus()
            return false
        }
        else if (v.isEmpty(tv_dob)) {
            Constant.snackbar(mainLayout, "Date of birth can't be empty")
            tv_dob.requestFocus()
            return false
        }
        else if (v.isEmpty(account_num)) {
            Constant.snackbar(mainLayout, "Account number can't be empty")
            account_num.requestFocus()
            return false
        }
        else if (v.isEmpty(address)) {
            Constant.snackbar(mainLayout, "Address can't be empty")
            address.requestFocus()
            return false
        }
        else if (v.isEmpty(postal_code)) {
            Constant.snackbar(mainLayout, "Postal code can't be empty")
            postal_code.requestFocus()
            return false
        }
        else if (v.isEmpty(ssn_last)) {
            Constant.snackbar(mainLayout, "SSN_Last can't be empty")
            ssn_last.requestFocus()
            return false
        }
        else if (v.isEmpty(routing_num)) {
            Constant.snackbar(mainLayout, "Routing number can't be empty")
            routing_num.requestFocus()
            return false
        }
        return true
    }
}
