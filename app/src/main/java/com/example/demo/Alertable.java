package com.example.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;


public interface Alertable {
    default void displayAlert(String message, Context context, boolean finishActivity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Ошибка!")
                .setMessage(message)
                .setCancelable(true)
                .setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        if (finishActivity) {
                            ((Activity) context).finish();
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
