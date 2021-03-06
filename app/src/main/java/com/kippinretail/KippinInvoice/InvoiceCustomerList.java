package com.kippinretail.KippinInvoice;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kippinretail.ApplicationuUlity.CommonUtility;
import com.kippinretail.ApplicationuUlity.ErrorCodes;
import com.kippinretail.ApplicationuUlity.Log;
import com.kippinretail.CommonDialog.CommonDialog;
import com.kippinretail.KippinInvoice.InvoiceAdapter.AdapterForCustomerList;
import com.kippinretail.Modal.Invoice.CustomerList;
import com.kippinretail.Modal.Invoice.EditCustomer;
import com.kippinretail.R;
import com.kippinretail.SuperActivity;
import com.kippinretail.interfaces.ButtonYesNoListner;
import com.kippinretail.loadingindicator.LoadingBox;
import com.kippinretail.retrofit.RestClient;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by kamaljeet.singh on 11/11/2016.
 */
public class InvoiceCustomerList extends SuperActivity implements View.OnClickListener {


    public int position;
    EditText etEmail;
    Button btn_search;
    ListView listForCutomerList;
    List<CustomerList> customerLists;
    EditText etSearch;
    TextView titleText, textView4;
    private Activity mActivity;
    private Dialog lDialog;
    private String roleId, supplier = null;
    private AdapterForCustomerList adapter;
    private String RoleIdUser;
    private boolean updateList = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_list_activity);
        initlization();

    }

    private void initlization() {

        etEmail = (EditText) findViewById(R.id.etEmail);
        btn_search = (Button) findViewById(R.id.btn_search);
        btn_search.setOnClickListener(this);
        // ListView
        listForCutomerList = (ListView) findViewById(R.id.listForCutomerList);
        titleText = (TextView) findViewById(R.id.titleText);
        textView4 = (TextView) findViewById(R.id.textView4);
        // Getting role ID weather user is Supplier or Customer
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            roleId = extras.getString("roleId");
            supplier = extras.getString("supplier");
            if (roleId.equals("2")) {
                generateRightAddButton(getString(R.string.customer_list_title), roleId);
                titleText.setText("Select Customer to Update details");
                textView4.setText("Enter Customer email to establish link");
                RoleIdUser = Customer = "2";
                showDialogMessage("Do you want to: Create/Update Customer list?");
            } else {
                if (supplier != null) {
                    if (supplier.equals("supplier")) {
                        generateRightAddButton(getString(R.string.supplier_list_title), roleId);
                        titleText.setText("Select Supplier to Update details");
                        textView4.setText("Enter Supplier email to establish link");

                        showDialogMessage("Do you want to: Create/Update Supplier list?");
                        //showDialogMessage("Do you want to: Create/Update Customer list?");

                        RoleIdUser = Customer = "1";
                    } else {
                        generateRightAddButton(getString(R.string.customer_list_title), roleId);
                        //titleText.setText("Select Supplier to Update details");
                        //textView4.setText("Enter Supplier email to establish link");
                        titleText.setText("Select Customer to Update details");
                        textView4.setText("Enter Customer email to establish link");
                        //showDialogMessage("Do you want to: Create/Update Supplier list?");
                        showDialogMessage("Do you want to: Create/Update Customer list?");

                        RoleIdUser = Customer = "1";
                    }
                } else {
                    generateRightAddButton(getString(R.string.customer_list_title), roleId);
                    //titleText.setText("Select Supplier to Update details");
                    //textView4.setText("Enter Supplier email to establish link");
                    titleText.setText("Select Customer to Update details");
                    textView4.setText("Enter Customer email to establish link");
                    //showDialogMessage("Do you want to: Create/Update Supplier list?");
                    showDialogMessage("Do you want to: Create/Update Customer list?");

                    RoleIdUser = Customer = "1";
                }

            }


        } else {
            generateRightText1(getString(R.string.customer_list_title));
            RoleIdUser = "2";
            //adapter = new AdapterForCustomerList(InvoiceCustomerList.this, customerLists, roleId);
            //  listForCutomerList.setAdapter(adapter);
            getCustomerList();

        }


        // Locate the EditText in listview_main.xml
        etSearch = (EditText) findViewById(R.id.etSearch);

        // Capture Text in EditText
        etSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
                // TODO Auto-generated method stub
                //   if (customerLists!=null&&customerLists.size()!=0) {

                String text = etSearch.getText().toString().toLowerCase();
                adapter.filter(text);

                //  }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("updateList:", "" + updateList);
        if (updateList) {
            etEmail.setText("");
            getCustomerList();
        }

    }

    private void showDialogMessage(String s) {

        CommonDialog.DialogToCreateuser(InvoiceCustomerList.this, s, new ButtonYesNoListner() {
            @Override
            public void YesButton() {
                updateList = true;
                getCustomerList();
            }

            @Override
            public void NoButton() {
                onBackPressed();
                overridePendingTransition(com.kippin.kippin.R.anim.animation_from_left, com.kippin.kippin.R.anim.animation_to_right);
            }
        });


    }


    private void getCustomerList() {

        Log.e("HERE", "HERE");
        LoadingBox.showLoadingDialog(activity, "");
        String sRoleid;
        if (RoleIdUser.equals("1")) {
            if (supplier != null) {
                if (supplier.equals("supplier")) {
                    sRoleid = "1";
                } else {
                    sRoleid = "2";
                }
            } else {
                sRoleid = "2";
            }

        } else {
            sRoleid = "2";
        }
        RestClient.getApiFinanceServiceForPojo().getCustomerList(getUserId(), sRoleid, new Callback<JsonElement>() {
                    @Override
                    public void success(JsonElement jsonElement, Response response) {
                        LoadingBox.dismissLoadingDialog();

                        Log.e("RestClient", jsonElement.toString() + " \n " + response.getUrl());
                        Gson gson = new Gson();
                        customerLists = gson.fromJson(jsonElement.toString(), new TypeToken<List<CustomerList>>() {
                        }.getType());
                        if (customerLists.get(0).getResponseCode() == 1) {
                            adapter = new AdapterForCustomerList(InvoiceCustomerList.this, customerLists, roleId);
                            listForCutomerList.setAdapter(adapter);
                        } else {
                            CommonDialog.With(InvoiceCustomerList.this).Show(customerLists.get(0).getResponseMessage());
                            return;
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        LoadingBox.dismissLoadingDialog();
                        Log.e("RestClient", error.getUrl() + "");
                    }
                }

        );

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search:
                if (etEmail.getText().toString().length() != 0) {
                    if (emailValidator1(etEmail.getText().toString())) {
                        if (Customer.equals("2")) {
                            searchEmailList();
                        } else {
                            searchSupplierList();
                        }
                    } else {
                        CommonDialog.With(InvoiceCustomerList.this).Show(getResources().getString(R.string.plz_enter_valide_email_id));
                    }
                } else {
                    CommonDialog.With(InvoiceCustomerList.this).Show("Please enter Email");
                }
                break;
        }
    }

    /**
     * @param message
     */
    public void ShowSupplierCustomer(String message) {
        try {
            final Dialog dialog = new Dialog(activity);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_custom_msg);
            TextView textMessage = (TextView) dialog.findViewById(R.id.text_msg);
            textMessage.setMovementMethod(new ScrollingMovementMethod());
            textMessage.setText(message);
            Button btnOk = (Button) dialog.findViewById(R.id.btnOk);

            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etEmail.setText("");
                    getCustomerList();
                    dialog.dismiss();
                }

            });

            dialog.show();
        } catch (Exception e) {
            Toast.makeText(activity, e.getMessage() + "", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void searchEmailList() {
        HashMap templatePosts = new HashMap();
        templatePosts.put("Email", etEmail.getText().toString());
        templatePosts.put("RoleId", Customer);
        templatePosts.put("UserId", getUserId());

        LoadingBox.showLoadingDialog(InvoiceCustomerList.this, "");
        RestClient.getApiFinanceServiceForPojo().CheckInvoiceExist(getUserId(), etEmail.getText().toString(), new Callback<JsonElement>() {
            @Override
            public void success(JsonElement element, Response response) {
                LoadingBox.dismissLoadingDialog();
                Log.e("Amit", "Request data " + new Gson().toJson(element));
//{"Id":0,"ResponseCode":3,"ResponseMessage":"Already Registered."}
                JsonObject jsonObj = element.getAsJsonObject();
                String strObj = jsonObj.toString();
                try {
                    JSONObject OBJ = new JSONObject(strObj);
                    String ResponseMessage = OBJ.getString("ResponseMessage");
                    String ResponseCode = OBJ.getString("ResponseCode");
                    if (ResponseCode.equals("1")) {
                        //callNext();
                        ShowSupplierCustomer(ResponseMessage);
                        // CommonDialog.With(InvoiceCustomerList.this).ShowSupplierCustomer(etEmail.getText().toString() + " " + ResponseMessage);
                    } else if (ResponseCode.equals("9")) {
                        CommonDialog.With(InvoiceCustomerList.this).Show(ResponseMessage);
                    } else if (ResponseCode.equals("8")) {
                        CommonDialog.DialogToCreateuser(InvoiceCustomerList.this,
                                "Customer does not exist.Do You want to create a new customer?", new ButtonYesNoListner() {
                                    @Override
                                    public void YesButton() {
                                        Intent in = new Intent();
                                        in.setClass(InvoiceCustomerList.this, CreateInvoiceCustomer.class);
                                        if (CommonUtility.InvoiceTitle.equals("1")) {
                                            in.putExtra("roleId", "2");
                                        } else {
                                            in.putExtra("onlysupplier", "customer");
                                            in.putExtra("roleId", "6");
                                        }

                                        startActivity(in);
                                        overridePendingTransition(R.anim.animation_from_right, R.anim.animation_to_left);
                                    }

                                    @Override
                                    public void NoButton() {

                                    }
                                });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("user updated exception response", " = " + e.toString());
                }


            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Failure ", " = " + error.getMessage());
                LoadingBox.dismissLoadingDialog();
                ErrorCodes.checkCode(InvoiceCustomerList.this, error);
            }
        });

    }

    private void searchSupplierList() {
        HashMap templatePosts = new HashMap();
        templatePosts.put("Email", etEmail.getText().toString());
        templatePosts.put("RoleId", Customer);
        templatePosts.put("UserId", getUserId());

        LoadingBox.showLoadingDialog(InvoiceCustomerList.this, "");
        RestClient.getApiFinanceServiceForPojo().CheckInvoiceSupplierExist(getUserId(), etEmail.getText().toString(), new Callback<JsonElement>() {
            @Override
            public void success(JsonElement element, Response response) {
                LoadingBox.dismissLoadingDialog();
                Log.e("Tag", "Request data " + new Gson().toJson(element));
//{"Id":0,"ResponseCode":3,"ResponseMessage":"Already Registered."}
                JsonObject jsonObj = element.getAsJsonObject();
                String strObj = jsonObj.toString();
                try {
                    JSONObject OBJ = new JSONObject(strObj);
                    String ResponseMessage = OBJ.getString("ResponseMessage");
                    String ResponseCode = OBJ.getString("ResponseCode");
                    if (ResponseCode.equals("1")) {
                        // callNext();
                        ShowSupplierCustomer(ResponseMessage);
                        //  CommonDialog.With(InvoiceCustomerList.this).ShowSupplierCustomer(etEmail.getText().toString() + " " + ResponseMessage);

                    } else if (ResponseCode.equals("3")) {
                        CommonDialog.With(InvoiceCustomerList.this).Show(ResponseMessage);
                    } else {
                        CommonDialog.DialogToCreateuser(InvoiceCustomerList.this,
                                "Supplier does not exist.Do You want to create a new supplier?", new ButtonYesNoListner() {
                                    @Override
                                    public void YesButton() {
                                        Intent in = new Intent();
                                        in.setClass(InvoiceCustomerList.this, CreateInvoiceCustomer.class);
                                        if (CommonUtility.InvoiceTitle.equals("1")) {
                                            Log.e("ifhere", "ifHere");
                                            in.putExtra("roleId", "2");
                                        } else {
                                            Log.e("elsehere", "elseHere");
                                            in.putExtra("onlysupplier", "onlysupplier");
                                            in.putExtra("roleId", "6");
                                        }

                                        startActivity(in);
                                        overridePendingTransition(R.anim.animation_from_right, R.anim.animation_to_left);
                                    }

                                    @Override
                                    public void NoButton() {

                                    }
                                });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("user updated exception response", " = " + e.toString());
                }


            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Failure ", " = " + error.getMessage());
                LoadingBox.dismissLoadingDialog();
                ErrorCodes.checkCode(InvoiceCustomerList.this, error);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 011) {
            EditCustomer editCustomer = (EditCustomer) data.getSerializableExtra("data");
            updateRecord(editCustomer);
        }
    }


    public void updateRecord(EditCustomer editCustomer) {
        for (int i = 0; i < customerLists.size(); i++) {
            if (customerLists.get(i).getCustomerUserMappingId().equals(editCustomer.getCustomerUserMappingId())) {

                customerLists.get(i).setCompanyName(editCustomer.getCompanyName());
                customerLists.get(i).setContactPerson(editCustomer.getContactPerson());
                customerLists.get(i).setAptNo(editCustomer.getAptNo());
                customerLists.get(i).setHouseNo(editCustomer.getHouseNo());
                customerLists.get(i).setCity(editCustomer.getCity());
                customerLists.get(i).setState(editCustomer.getState());
                customerLists.get(i).setPostalCode(editCustomer.getPostalCode());
                customerLists.get(i).setMobile(editCustomer.getMobile());
                customerLists.get(i).setEmail(editCustomer.getEmail());
                customerLists.get(i).setWebsite(editCustomer.getWebsite());
                customerLists.get(i).setAdditionalEmail(editCustomer.getAdditionalEmail());
                customerLists.get(i).setShippingAptNo(editCustomer.getShippingAptNo());
                customerLists.get(i).setShippingHouseNo(editCustomer.getShippingHouseNo());
                customerLists.get(i).setShippingCity(editCustomer.getShippingCity());
                customerLists.get(i).setShippingState(editCustomer.getShippingState());
                customerLists.get(i).setShippingPostalCode(editCustomer.getShippingPostalCode());
                customerLists.get(i).setTelephone(editCustomer.getTelephone());
                customerLists.get(i).setIsFinanceUser(editCustomer.getIsFinanceUser());
                adapter.notifyDataSetChanged();
            }
        }
    }

}
