package ie.app.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ie.app.activities.R;
import ie.app.api.DonationApi;
import ie.app.main.DonationApp;
import ie.app.models.Donation;

public class Donate extends Base {

    private Button          donateButton;
    private RadioGroup      paymentMethod;
    private ProgressBar     progressBar;
    private NumberPicker    amountPicker;
    private EditText        amountText;
    private TextView        amountTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        donateButton = (Button) findViewById(R.id.donateButton);

        paymentMethod = (RadioGroup)   findViewById(R.id.paymentMethod);
        progressBar   = (ProgressBar)  findViewById(R.id.progressBar);
        amountPicker  = (NumberPicker) findViewById(R.id.amountPicker);
        amountText    = (EditText)     findViewById(R.id.paymentAmount);
        amountTotal   = (TextView)     findViewById(R.id.totalSoFar);

        amountPicker.setMinValue(0);
        amountPicker.setMaxValue(1000);
        progressBar.setMax(app.target);
        amountTotal.setText("$0");
    }

    public void donateButtonPressed (View view)
    {
        String method = paymentMethod.getCheckedRadioButtonId() ==
                R.id.PayPal ? "PayPal" : "Direct";
        int donatedAmount = amountPicker.getValue();

        if (donatedAmount == 0)
        {
            String text = amountText.getText().toString();
            boolean check = Integer.parseInt(text) + DonationApp.totalDonated < DonationApp.target;
            if (!text.equals(""))
                if (check) {
                    donatedAmount = Integer.parseInt(text);
                } else {
                    Toast.makeText(this, "Can not add!", Toast.LENGTH_LONG).show();
                }
        }
        if (donatedAmount > 0)
        {
            Date newDate = new Date();
            SimpleDateFormat sm = new SimpleDateFormat("MM/dd/yyyy");
            String strDate = sm.format(newDate);
            Donation newDonation = new Donation(String.valueOf(app.donations.size()), donatedAmount, method, 0, strDate);
            if (DonationApp.totalDonated < DonationApp.target) {
                new InsertTask(this).execute("/donations", newDonation);

                progressBar.setProgress(DonationApp.totalDonated);
                String totalDonatedStr = "$" + DonationApp.totalDonated;
                amountTotal.setText(totalDonatedStr);
            } else {
                progressBar.setProgress(DonationApp.target);
                String totalDonatedStr = "$" + DonationApp.target;
                amountTotal.setText(totalDonatedStr);
            }
            progressBar.setProgress(DonationApp.totalDonated);
            String totalDonatedStr = "$" + DonationApp.totalDonated;
            amountTotal.setText(totalDonatedStr);
        }
    }

    private class InsertTask extends AsyncTask<Object, Void, String> {
        protected ProgressDialog dialog;
        protected Context context;
        public InsertTask(Context context)
        {
            this.context = context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Saving Donation....");
            this.dialog.show();
        }
        @Override
        protected String doInBackground(Object... params) {
            String res = null;
            try {
                Log.v("donate", "Donation App Inserting");
                res = (String) DonationApi.insert((String) params[0], (Donation) params[1]);
            }
            catch(Exception e)
            {
                Log.v("donate","ERROR : " + e);
                e.printStackTrace();
            }
            return res;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new GetAllTask(Donate.this).execute("/donations");
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }

    private class GetAllTask extends AsyncTask<String, Void, List<Donation>> {
        protected ProgressDialog dialog;
        protected Context context;
        public GetAllTask(Context context)
        {
            this.context = context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Retrieving Donations List");
            this.dialog.show();
        }
        @Override
        protected List<Donation> doInBackground(String... params) {
            try {
                Log.v("Donate", "Donation App Getting All Donations");
                return (List<Donation>) DonationApi.getAll((String) params[0]);
            }
            catch (Exception e) {
                Log.v("donate", "ERROR : " + e);
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(List<Donation> result) {
            super.onPostExecute(result);
            app.donations = result;
            app.getTotalDonated();
            progressBar.setProgress(app.totalDonated);
            amountTotal.setText("$" + app.totalDonated);
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }

    private class ResetTask extends AsyncTask<Object, Void, String> {
        protected ProgressDialog dialog;
        protected Context context;
        public ResetTask(Context context)
        {
            this.context = context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new ProgressDialog(context, 1);
            this.dialog.setMessage("Reset Donations....");
            this.dialog.show();
        }
        @Override
        protected String doInBackground(Object... params) {
            String res = null;
            try {
                res = DonationApi.deleteAll((String)params[0]);
            }
            catch(Exception e)
            {
                Log.v("donate"," RESET ERROR : " + e);
                e.printStackTrace();
            }
            return res;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            app.totalDonated = 0;
            progressBar.setProgress(app.totalDonated);
            amountTotal.setText("$" + app.totalDonated);
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetAllTask(this).execute("/donations");
    }

    @Override
    public void reset(MenuItem item) {
        super.reset(item);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset donations?");
        builder.setIcon(android.R.drawable.ic_delete);
        builder.setMessage("Are you sure you want to reset all the donations ?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                new ResetTask(Donate.this).execute("/donations");
                app.totalDonated = 0;
                amountTotal.setText("$" + app.totalDonated);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

}


