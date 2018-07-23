package com.togocourier.ui.activity.customer

import android.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.togocourier.R
import kotlinx.android.synthetic.main.activity_payment.*
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.Window
import android.widget.*
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.year_month_dialog.*
import java.io.IOException
import java.util.*
import com.google.gson.Gson
import com.togocourier.responceBean.PaymentResponce
import com.togocourier.ui.activity.HomeActivity
import com.togocourier.util.*
import com.togocourier.vollyemultipart.VolleySingleton
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset


class CustmorPaymentActivity : AppCompatActivity() {

    private var strcardNum1 =""
    private var strcardNum2 =""
    private var strcardNum3 =""
    private var strcardNum4 =""
    private var TotalcardNum =""

    private var requestId = ""
    private var bitPrice = ""
    var expireMnth = ""
    var expireYear = ""
    var stateName = ""
    var cityName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        if(intent != null ){
            val bundle : Bundle  = intent.extras
            requestId = bundle!!.getString("requestId")
            bitPrice = bundle!!.getString("bitPrice")
            print(requestId)
        }


        iv_back_press.setOnClickListener {
            onBackPressed()
        }



        pay_from_card.text = "Pay $"+bitPrice

        pay_from_card.setOnClickListener {
            var cvv = ed_cvv.text.toString()
            TotalcardNum = strcardNum1+strcardNum2+strcardNum3+strcardNum4
            var cardHolderName = cardHolderName.text.toString()

            if(isCardVaild()){
                cardPay(expireMnth,expireYear,cvv,TotalcardNum,cardHolderName,stateName,
                        "","",cityName,"","","",""
                        ,"","","1")

            }
        }

        getCardComponents()

        tv_date.setOnClickListener{
            showMonnthYearDialog()
        }
    }



    fun getCardComponents(){
        var cardHolderName = cardHolderName.text.toString().trim()

        cardNum1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (cardNum1.getText().length == 4) {
                    cardNum2.requestFocus()

                }
                strcardNum1 = cardNum1.text.toString()

            }
        })

        cardNum2.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (cardNum2.getText().length == 4) {
                    cardNum3.requestFocus()
                }
                strcardNum2 = cardNum2.text.toString()
            }
        })

        cardNum3.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (cardNum3.getText().length == 4) {
                    cardNum4.requestFocus()
                }
                strcardNum3 = cardNum3.text.toString()
            }
        })

        cardNum4.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                strcardNum4 = cardNum4.text.toString()
            }

        })
    }

    fun showMonnthYearDialog() {
        val width = windowManager.defaultDisplay.getWidth()
        val d = Dialog(this)
        d.requestWindowFeature(Window.FEATURE_NO_TITLE)
        d.setContentView(R.layout.year_month_dialog)
        d.getWindow().setLayout(width * 9 / 10, LinearLayout.LayoutParams.WRAP_CONTENT)

        val year = Calendar.getInstance().get(Calendar.YEAR)
        val month = Calendar.getInstance().get(Calendar.MONTH)
        d.monthPicker.minValue = 1
        d.monthPicker.maxValue = 12
        d.monthPicker.value = month + 1
        d.yearPicker.maxValue = year + 20
        d.yearPicker.minValue = year
        d.yearPicker.wrapSelectorWheel = false
        d.yearPicker.value = year
        d.yearPicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        d.button1.setOnClickListener(View.OnClickListener {
            var date =  (d.monthPicker.value.toString() + "/" + d.yearPicker.value.toString())

            expireMnth = d.monthPicker.value.toString()
            expireYear = d.yearPicker.value.toString().substring(2, 4)

            var expireMnth = if (expireMnth.toInt() < 10)
            {
                "0" + expireMnth.toInt() }

            else {
                expireMnth
            }

            tv_date.text = expireMnth+"/"+expireYear
            d.dismiss()
        })
        d.button2.setOnClickListener(View.OnClickListener { d.dismiss() })
        d.show()
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


    fun cardPay(exp_month: String, exp_year: String, cvv: String,
                totalcardNum: String, cardHolderName: String,state:String,
                address:String,postalCode:String,city:String,accountNo:String,routingNumber:String,
                currency:String,country:String,dob:String,ssnLast:String,payType:String) {

        if (Constant.isNetworkAvailable(this, mainLayout)) {
            progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.StripPay,
                    Response.Listener { response ->
                        progressBar.visibility = View.GONE
                        println("#" + response)
                        Log.e("Stripes", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")

                            if(status.equals("return")){
                                Constant.returnAlertDialogToMainActivity(this@CustmorPaymentActivity,message)
                                return@Listener
                            }

                            if (status == "success") {
                                var gson = Gson()
                                var paymentInfo = gson.fromJson(response, PaymentResponce::class.java)

                                //AddCardSuccess(paymentInfo.message.toString())
                                acceptRejectRequest(requestId,"accept")
                            } else {

                                var userType = PreferenceConnector.readString(this,PreferenceConnector.USERTYPE,"")
                                if(userType.equals(Constant.COURIOR)){
                                    if(message.equals("Currently you are inactivate user")){
                                        var helper = HelperClass(this,this)
                                        helper.inActiveByAdmin("Admin inactive your account",true)
                                    }else{
                                        Constant.snackbar(mainLayout, "You have entered wrong parameter")
                                    }
                                }else{
                                    Constant.snackbar(mainLayout, "You have entered wrong parameter")
                                }


                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {


                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@CustmorPaymentActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("email",PreferenceConnector.readString(this@CustmorPaymentActivity, PreferenceConnector.USEREMAIL, "") )
                    params.put("amount", bitPrice)
                    params.put("requestId", requestId)
                    params.put("payType", payType)
                    params.put("saveDetail", "2")
                    params.put("firstName", cardHolderName)
                    params.put("lastName", cardHolderName)
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
                    params.put("card_number",totalcardNum)
                    params.put("exp_month", exp_month)
                    params.put("exp_year", exp_year)
                    params.put("cvv", cvv)
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


    fun isCardVaild(): Boolean {
        val v = Validation()

        if (v.isEmpty(cardHolderName)) {
            Constant.snackbar(mainLayout, "Cardholder Name can't be empty")
            cardHolderName.requestFocus()
            return false
        }
        else if (v.isEmpty(cardNum1)) {
            Constant.snackbar(mainLayout, "Card number can't be empty")
            cardNum1.requestFocus()
            return false
        }
        else if (v.isEmpty(cardNum2)) {
            Constant.snackbar(mainLayout, "Card number can't be empty")
            cardNum2.requestFocus()
            return false
        }
        else if (v.isEmpty(cardNum3)) {
            Constant.snackbar(mainLayout, "Card number can't be empty")
            cardNum3.requestFocus()
            return false
        }
        else if (v.isEmpty(cardNum4)) {
            Constant.snackbar(mainLayout, "Card number can't be empty")
            cardNum4.requestFocus()
            return false
        }
        else if (v.isEmpty(tv_date)) {
            Constant.snackbar(mainLayout, "Expiry date can't be empty")
            tv_date.requestFocus()
            return false
        }
        else if (v.isEmpty(ed_cvv)) {
            Constant.snackbar(mainLayout, "CVV number can't be empty")
            ed_cvv.requestFocus()
            return false
        }
        return true
    }



    fun AddCardSuccess(msg:String) {

        val alertDialog = AlertDialog.Builder(this)

        alertDialog.setTitle("Alert")

        alertDialog.setCancelable(false)
        alertDialog.setMessage(msg)

        alertDialog.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
            var intent =  Intent(this,HomeActivity::class.java)
            startActivity(intent)
            finish()

            this.finish()
        })
        alertDialog.show()

    }


    private fun acceptRejectRequest(id: String, status: String) {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.Accept_reject_Request_Url,
                    Response.Listener { response ->
                        progressBar.visibility = View.GONE
                        println("#" + response)
                        Log.e("GETAllREQUEST", "onResponse: " + response)
                        var result: JSONObject? = null
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if(status.equals("return")){
                                Constant.returnAlertDialog(this@CustmorPaymentActivity,message)
                                return@Listener
                            }

                            if (status == "success") {

                                AddCardSuccess("Payment has done successfully")

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
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {

                /* @Override
            public Map<String, String> getParams() throws AuthFailureError {
                SimpleDateFormat writeFormat = new SimpleDateFormat("yyyy-MM-dd");
                String todayDate = writeFormat.format(date);
                Map<String,String> header = new HashMap<>();
                header.put("month", todayDate);
                return header;
            }*/

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@CustmorPaymentActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("requestId", id)
                    params.put("requestStatus", "accept")
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
