/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.talkbacktests.testsession;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.android.talkbacktests.R;

public class ValuePickerTest extends BaseTestContent implements View.OnClickListener,
        NumberPicker.OnValueChangeListener, DialogInterface.OnClickListener {

    private AlertDialog mNumberPickerDialog;

    private TextView mDurationSummary;
    private NumberPicker npHour;
    private NumberPicker npMinute;

    public ValuePickerTest(Context context, String subtitle, String description) {
        super(context, subtitle, description);
    }

    @Override
    public View getView(final LayoutInflater inflater, ViewGroup container, Context context) {
        View view = inflater.inflate(R.layout.test_value_picker, container, false);
        view.findViewById(R.id.test_number_picker_button).setOnClickListener(this);
        mDurationSummary = (TextView) view.findViewById(R.id.test_number_picker_description);

        mDurationSummary.setText("(no duration specified)");

        View pickerDurationView = inflater.inflate(R.layout.test_value_picker_duration, container, false);
        npHour = (NumberPicker) pickerDurationView.findViewById(R.id.test_duration_picker_hour_picker);
        npHour.setMaxValue(12);
        npHour.setMinValue(0);
        npHour.setValue(0);
        npHour.setWrapSelectorWheel(false);
        npHour.setOnValueChangedListener(this);

        npMinute = (NumberPicker) pickerDurationView.findViewById(R.id.test_duration_picker_minute_picker);
        npMinute.setMaxValue(59);
        npMinute.setMinValue(0);
        npMinute.setValue(0);
        npMinute.setWrapSelectorWheel(true);
        npMinute.setOnValueChangedListener(this);

        // Set contentDescription for the label, to associate it with the picker below it,
        //  and the contentDescription for the picker, which isn't actually used by TalkBack
        //  so we use it when the value of the picker is changed (below)
        TextView hourLabel = (TextView) pickerDurationView.findViewById(R.id.test_duration_picker_hour_label);
        hourLabel.setContentDescription(hourLabel.getText() + ". " + getString(R.string.picker_label_content_description));
        npHour.setContentDescription(hourLabel.getText());

        TextView minuteLabel = (TextView) pickerDurationView.findViewById(R.id.test_duration_picker_minute_label);
        minuteLabel.setContentDescription(minuteLabel.getText() + ". " + getString(R.string.picker_label_content_description));
        npMinute.setContentDescription(minuteLabel.getText());

        // Setting a LabelFor (in the layout XML) or LabeledBy (here) for a NumberPicker doesn't seem to work:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            final View fHoursLabel = hourLabel;
            View.AccessibilityDelegate hoursDelegate = new View.AccessibilityDelegate() {
                @Override
                public void onInitializeAccessibilityNodeInfo(View host,
                                                              AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                    info.setLabeledBy(fHoursLabel);
                }
            };
            npHour.setAccessibilityDelegate(hoursDelegate);
            final View fMinutesLabel = minuteLabel;
            View.AccessibilityDelegate minutesDelegate = new View.AccessibilityDelegate() {
                @Override
                public void onInitializeAccessibilityNodeInfo(View host,
                                                              AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                    info.setLabeledBy(fMinutesLabel);
                }
            };
            npMinute.setAccessibilityDelegate(minutesDelegate);
        } else {
            TextView warning = (TextView) view.findViewById(R.id.test_label_warning);
            warning.setText(getString(R.string.min_api_level_warning,
                    Build.VERSION_CODES.JELLY_BEAN_MR1,
                    Build.VERSION.SDK_INT));
            hourLabel.setEnabled(false);
            npHour.setEnabled(false);
            minuteLabel.setEnabled(false);
            npMinute.setEnabled(false);
        }

        // Create the actual picker dialog as an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.number_picker_title);
        builder.setView(pickerDurationView);
        builder.setPositiveButton(R.string.alert_ok_button, this);
        builder.setNegativeButton(R.string.alert_cancel_button, this);
        mNumberPickerDialog = builder.create();
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.test_number_picker_button:
                mNumberPickerDialog.show();
                break;
        }
    }

    @Override
    public void onClick(DialogInterface di, int button) {
        switch (button) {
            case DialogInterface.BUTTON_POSITIVE:
                mDurationSummary.setText(npHour.getValue() + " " + npHour.getContentDescription() + " , " + npMinute.getValue() + " " + npMinute.getContentDescription());
                mDurationSummary.announceForAccessibility("Debug: Value set to: " + npHour.getValue() + " " + npHour.getContentDescription() + " , " + npMinute.getValue() + " " + npMinute.getContentDescription());
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                mDurationSummary.announceForAccessibility("Debug: Dialog cancelled - hours and minutes not updated");
                break;
        }
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
        // Since labeling a NumberPicker doesn't seem to work, announce the new value and the label
        // (the label is stored as the ContentDescription of the NumberPicker)
        numberPicker.announceForAccessibility(newVal + " " + numberPicker.getContentDescription());
    }
}