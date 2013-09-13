package com.syn.mpos;

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class MyKeyboard {

	private KeyboardView mKeyboardView;
	private Activity mHostActivity;

	private OnKeyboardActionListener mOnKeyboardActionListener = new OnKeyboardActionListener() {

		@Override
		public void onKey(int primaryCode, int[] keyCodes) {
			View currFocus = mHostActivity.getCurrentFocus();
			if (currFocus == null || currFocus.getClass() != EditText.class)
				return;

			EditText editText = (EditText) currFocus;
			Editable editAble = editText.getText();
			int start = editText.getSelectionStart();

			switch (primaryCode) {
			case Keyboard.KEYCODE_CANCEL:

				break;
			case Keyboard.KEYCODE_DELETE:
				if (editAble != null) {
					if(start > 0)
						editAble.delete(start - 1, start);
				}
				break;
			default:
				editAble.insert(start, Character.toString((char) primaryCode));
				break;
			}
		}

		@Override
		public void onPress(int primaryCode) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onRelease(int primaryCode) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onText(CharSequence text) {
			// TODO Auto-generated method stub

		}

		@Override
		public void swipeDown() {
			// TODO Auto-generated method stub

		}

		@Override
		public void swipeLeft() {
			// TODO Auto-generated method stub

		}

		@Override
		public void swipeRight() {
			// TODO Auto-generated method stub

		}

		@Override
		public void swipeUp() {
			// TODO Auto-generated method stub

		}

	};
	
	public MyKeyboard(Activity host, int viewId, int layoutId){
		mHostActivity = host;
		mKeyboardView = (KeyboardView) mHostActivity.findViewById(viewId);
		mKeyboardView.setKeyboard(new Keyboard(mHostActivity, layoutId));
		mKeyboardView.setPreviewEnabled(false); // NOTE Do not show the preview
												// balloons
		mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
		// Hide the standard keyboard initially
		mHostActivity.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);	
	}

	/** Returns whether the CustomKeyboard is visible. */
	public boolean isCustomKeyboardVisible() {
		return mKeyboardView.getVisibility() == View.VISIBLE;
	}
	
	/**
	 * Make the CustomKeyboard visible, and hide the system keyboard for view v.
	 */
	public void showCustomKeyboard(View v) {
		mKeyboardView.setVisibility(View.VISIBLE);
		mKeyboardView.setEnabled(true);
		if (v != null)
			((InputMethodManager) mHostActivity
					.getSystemService(Activity.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	/** Make the CustomKeyboard invisible. */
	public void hideCustomKeyboard() {
		mKeyboardView.setVisibility(View.GONE);
		mKeyboardView.setEnabled(false);
	}
	
	/**
	 * Register <var>EditText<var> with resource id <var>resid</var> (on the
	 * hosting activity) for using this custom keyboard.
	 * 
	 * @param resid
	 *            The resource id of the EditText that registers to the custom
	 *            keyboard.
	 */
	public void registerEditText(EditText editText) {

		// Make the custom keyboard appear
		editText.setOnFocusChangeListener(new OnFocusChangeListener() {
			// NOTE By setting the on focus listener, we can show the custom
			// keyboard when the edit box gets focus, but also hide it when the
			// edit box loses focus
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus)
					showCustomKeyboard(v);
				else
					hideCustomKeyboard();
			}
		});
		editText.setOnClickListener(new OnClickListener() {
			// NOTE By setting the on click listener, we can show the custom
			// keyboard again, by tapping on an edit box that already had focus
			// (but that had the keyboard hidden).
			@Override
			public void onClick(View v) {
				showCustomKeyboard(v);
			}
		});
		// Disable standard keyboard hard way
		// NOTE There is also an easy way:
		// 'edittext.setInputType(InputType.TYPE_NULL)' (but you will not have a
		// cursor, and no 'edittext.setCursorVisible(true)' doesn't work )
		editText.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				EditText edittext = (EditText) v;
				int inType = edittext.getInputType(); // Backup the input type
				edittext.setInputType(InputType.TYPE_NULL); // Disable standard
															// keyboard
				edittext.onTouchEvent(event); // Call native handler
				edittext.setInputType(inType); // Restore input type
				return true; // Consume touch event
			}
		});
		// Disable spell check (hex strings look like words to Android)
		editText.setInputType(editText.getInputType()
				| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
	}
}
