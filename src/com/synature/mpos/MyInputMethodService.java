package com.synature.mpos;

import android.inputmethodservice.InputMethodService;

public class MyInputMethodService extends InputMethodService{

	@Override
	public boolean onEvaluateInputViewShown() {
		return true;
	}
}
