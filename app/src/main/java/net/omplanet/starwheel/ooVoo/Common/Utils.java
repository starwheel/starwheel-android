//
// Utils.java
//
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license.
//
package net.omplanet.starwheel.ooVoo.Common;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.oovoo.core.Utils.MethodUtils;

import net.omplanet.starwheel.MainApplication;
import net.omplanet.starwheel.R;

public class Utils {

	public static String getCurrentMethodName(int i)
	{
		return MethodUtils.getCallingMethodName();
	}

	// Retrieve the ooVoo tag for log prints
	public static String getOoVooTag()
	{
		return MainApplication.getApplicationResources().getString(R.string.ooVooTag);
	}

	// Gets the requested spinner's value
	public static <T>  T getSelectedSpinnerValue(Spinner spinner) {
		return (T) spinner.getSelectedItem();
	}

	public static void printCurrentMethodNameToLog()
	{
		MethodUtils.printCurrentMethodNameToLog();
	}

	// Sets the requested spinner's value
	public static <T> void setSelectedSpinnerValue(Spinner spinner, T valueToSet) {
		ArrayAdapter<T> adapter = (ArrayAdapter<T>) spinner.getAdapter();
		int spinnerPosition = adapter.getPosition( valueToSet);
		spinner.setSelection( spinnerPosition);
	}

	// Sets the available spinner's values
	public static <T> void setSpinnerValues(Context context,Spinner spinner,
			List<T> values) {
		ArrayAdapter<T> adapter;
		adapter = new ArrayAdapter<T>(context,
				R.layout.spinner_item, values);
		spinner.setAdapter(adapter);
	}

	public static void ShowMessageBox(Context context,String title,String msg)
	{
		try {
			if( context != null) {
				AlertDialog.Builder popupBuilder = new AlertDialog.Builder(context);
				TextView myMsg = new TextView(context);
				myMsg.setText(msg);
				myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
				popupBuilder.setTitle(title);
				popupBuilder.setPositiveButton("OK", null);
				popupBuilder.setView(myMsg);

				popupBuilder.show();
			}
		} catch( Exception e) {
		}
	}
}
