// Generated by view binder compiler. Do not edit!
package com.example.hotelreservation.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.hotelreservation.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityMainBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final Button btnLogin;

  @NonNull
  public final CardView cardLogin;

  @NonNull
  public final EditText edtPassword;

  @NonNull
  public final EditText edtUsername;

  @NonNull
  public final ImageView imgLogo;

  @NonNull
  public final TextView txtAppTitle;

  @NonNull
  public final TextView txtErrorMessage;

  @NonNull
  public final TextView txtRegister;

  private ActivityMainBinding(@NonNull ConstraintLayout rootView, @NonNull Button btnLogin,
      @NonNull CardView cardLogin, @NonNull EditText edtPassword, @NonNull EditText edtUsername,
      @NonNull ImageView imgLogo, @NonNull TextView txtAppTitle, @NonNull TextView txtErrorMessage,
      @NonNull TextView txtRegister) {
    this.rootView = rootView;
    this.btnLogin = btnLogin;
    this.cardLogin = cardLogin;
    this.edtPassword = edtPassword;
    this.edtUsername = edtUsername;
    this.imgLogo = imgLogo;
    this.txtAppTitle = txtAppTitle;
    this.txtErrorMessage = txtErrorMessage;
    this.txtRegister = txtRegister;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityMainBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityMainBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_main, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityMainBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btnLogin;
      Button btnLogin = ViewBindings.findChildViewById(rootView, id);
      if (btnLogin == null) {
        break missingId;
      }

      id = R.id.cardLogin;
      CardView cardLogin = ViewBindings.findChildViewById(rootView, id);
      if (cardLogin == null) {
        break missingId;
      }

      id = R.id.edtPassword;
      EditText edtPassword = ViewBindings.findChildViewById(rootView, id);
      if (edtPassword == null) {
        break missingId;
      }

      id = R.id.edtUsername;
      EditText edtUsername = ViewBindings.findChildViewById(rootView, id);
      if (edtUsername == null) {
        break missingId;
      }

      id = R.id.imgLogo;
      ImageView imgLogo = ViewBindings.findChildViewById(rootView, id);
      if (imgLogo == null) {
        break missingId;
      }

      id = R.id.txtAppTitle;
      TextView txtAppTitle = ViewBindings.findChildViewById(rootView, id);
      if (txtAppTitle == null) {
        break missingId;
      }

      id = R.id.txtErrorMessage;
      TextView txtErrorMessage = ViewBindings.findChildViewById(rootView, id);
      if (txtErrorMessage == null) {
        break missingId;
      }

      id = R.id.txtRegister;
      TextView txtRegister = ViewBindings.findChildViewById(rootView, id);
      if (txtRegister == null) {
        break missingId;
      }

      return new ActivityMainBinding((ConstraintLayout) rootView, btnLogin, cardLogin, edtPassword,
          edtUsername, imgLogo, txtAppTitle, txtErrorMessage, txtRegister);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
