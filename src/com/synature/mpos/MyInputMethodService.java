package com.synature.mpos;

import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;

public class MyInputMethodService extends InputMethodService{

	@Override
	public boolean onEvaluateInputViewShown() {
		 Configuration config = getResources().getConfiguration();
	     return config.keyboard == Configuration.KEYBOARD_NOKEYS
	             || config.hardKeyboardHidden == Configuration.KEYBOARDHIDDEN_YES;
	}

}
