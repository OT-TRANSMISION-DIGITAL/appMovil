package com.example.transmisiondigital.drawing;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.example.transmisiondigital.R;

public class LoadingDialog {
    private Dialog dialog;

    public LoadingDialog(Context context) {
        dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        View view = LayoutInflater.from(context).inflate(R.layout.loading_dialog, null);
        dialog.setContentView(view);
        dialog.setCancelable(false);
    }

    public void show() {
        dialog.show();
    }

    public void hide() {
        dialog.dismiss();
    }
}
