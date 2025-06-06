// Generated by view binder compiler. Do not edit!
package com.example.hotelreservation.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.hotelreservation.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityNewReservationBinding implements ViewBinding {
  @NonNull
  private final ScrollView rootView;

  @NonNull
  public final Button btnCreateReservation;

  @NonNull
  public final DatePicker datePickerCheckIn;

  @NonNull
  public final DatePicker datePickerCheckOut;

  @NonNull
  public final EditText edtGuests;

  @NonNull
  public final Spinner spinnerHotel;

  @NonNull
  public final Spinner spinnerRoom;

  private ActivityNewReservationBinding(@NonNull ScrollView rootView,
      @NonNull Button btnCreateReservation, @NonNull DatePicker datePickerCheckIn,
      @NonNull DatePicker datePickerCheckOut, @NonNull EditText edtGuests,
      @NonNull Spinner spinnerHotel, @NonNull Spinner spinnerRoom) {
    this.rootView = rootView;
    this.btnCreateReservation = btnCreateReservation;
    this.datePickerCheckIn = datePickerCheckIn;
    this.datePickerCheckOut = datePickerCheckOut;
    this.edtGuests = edtGuests;
    this.spinnerHotel = spinnerHotel;
    this.spinnerRoom = spinnerRoom;
  }

  @Override
  @NonNull
  public ScrollView getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityNewReservationBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityNewReservationBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_new_reservation, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityNewReservationBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btnCreateReservation;
      Button btnCreateReservation = ViewBindings.findChildViewById(rootView, id);
      if (btnCreateReservation == null) {
        break missingId;
      }

      id = R.id.datePickerCheckIn;
      DatePicker datePickerCheckIn = ViewBindings.findChildViewById(rootView, id);
      if (datePickerCheckIn == null) {
        break missingId;
      }

      id = R.id.datePickerCheckOut;
      DatePicker datePickerCheckOut = ViewBindings.findChildViewById(rootView, id);
      if (datePickerCheckOut == null) {
        break missingId;
      }

      id = R.id.edtGuests;
      EditText edtGuests = ViewBindings.findChildViewById(rootView, id);
      if (edtGuests == null) {
        break missingId;
      }

      id = R.id.spinnerHotel;
      Spinner spinnerHotel = ViewBindings.findChildViewById(rootView, id);
      if (spinnerHotel == null) {
        break missingId;
      }

      id = R.id.spinnerRoom;
      Spinner spinnerRoom = ViewBindings.findChildViewById(rootView, id);
      if (spinnerRoom == null) {
        break missingId;
      }

      return new ActivityNewReservationBinding((ScrollView) rootView, btnCreateReservation,
          datePickerCheckIn, datePickerCheckOut, edtGuests, spinnerHotel, spinnerRoom);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
