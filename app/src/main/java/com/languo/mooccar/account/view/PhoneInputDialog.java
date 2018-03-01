package com.languo.mooccar.account.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.languo.mooccar.R;
import com.languo.mooccar.common.util.FormatUtil;

/**
 * Created by YuLiang on 2018/1/10.
 */

public class PhoneInputDialog extends Dialog {

    private View mRoot;
    private EditText mPhone;
    private Button mButton;

    public PhoneInputDialog(@NonNull Context context) {
        this(context, R.style.Dialog);
    }

    public PhoneInputDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        mRoot = layoutInflater.inflate(R.layout.dialog_phone_input,null);
        mRoot = LayoutInflater.from(getContext()).inflate(R.layout.dialog_phone_input,null);
        setContentView(mRoot);
        initListener();
    }


    /**
     * 监听点击事件
     */
    private void initListener() {
        mButton = (Button) mRoot.findViewById(R.id.btn_next);
        mButton.setEnabled(false);
        mPhone = (EditText) mRoot.findViewById(R.id.phone);
        mPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //检查是否是合法号码
                check();
            }
        });

        //按钮注册监听 -------> 显示验证码对话框
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                //弹出验证码输入框
                SmsCodeDialog smsCodeDialog = new SmsCodeDialog(getContext(), mPhone.getText().toString().trim());
                smsCodeDialog.show();
            }
        });
    }

    /**
     * 检查号码是否合法
     */
    private void check() {
        String phone = mPhone.getText().toString().trim();
        boolean legal = FormatUtil.checkMobile(phone);
        if(legal) {
            mButton.setEnabled(true);
        } else {
            mButton.setEnabled(false);
        }
    }
}
