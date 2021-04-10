package com.example.geolocationdemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;


public interface Alertable {
    default void displayAlert(String message, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Ошибка!")
                .setMessage(message)
                .setCancelable(true)
                .setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
