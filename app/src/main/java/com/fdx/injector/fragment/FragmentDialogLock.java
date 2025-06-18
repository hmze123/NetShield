package com.fdx.injector.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.Button;
import androidx.fragment.app.DialogFragment;
import com.fdx.injector.R;

public class FragmentDialogLock extends DialogFragment
 implements View.OnClickListener {

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);

	}
	
	@Override
	public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) {
	
		getDialog().setCanceledOnTouchOutside(false);
		getDialog().getWindow().setLayout((int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.8d), -2);

		return super.onCreateView(arg0, arg1, arg2);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstance){
		LayoutInflater li = LayoutInflater.from(getContext());
        View view = li.inflate(R.layout.c_c, null); 

        Button cancelButton = view.findViewById(R.id.btnCancelLck);
        
        cancelButton.setOnClickListener(this);
        
        return new AlertDialog.Builder(getActivity())   
            .setView(view)
            . show();
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
            case R.id.btnCancelLck:
                dismiss();
                break;
        }
	}

}