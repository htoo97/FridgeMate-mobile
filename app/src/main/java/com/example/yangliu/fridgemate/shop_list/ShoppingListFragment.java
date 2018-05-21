package com.example.yangliu.fridgemate.shop_list;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.yangliu.fridgemate.R;

import java.util.LinkedList;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class ShoppingListFragment extends Fragment {

    private LinearLayout llContentView;

    private EditText etContent1;
    private EditText etContent2;
    private int editIDIndex = 999;
    private int checkIDIndex = 1000;
    // “+”按钮控件List
    private LinkedList<ImageButton> listIBTNAdd;
    // “+”按钮ID索引
    private int btnIDIndex = 1000;
    // “-”按钮控件List
    private LinkedList<ImageButton> listIBTNDel;
    private LinkedList<CheckBox>listCBFinish;
    private LinkedList<TextView>listTVNumber;

    private int iETContentHeight = 0;   // EditText控件高度
    private int numberETHeight = 0;
    private float fDimRatio = 1.0f; // 尺寸比例（实际尺寸/xml文件里尺寸）

    private int iIndex = 0;

    private String currentText = "";
    private String currentNum = "1";

    private Button add;

    private CheckBox check;
    private ScrollView scroll;

    private ImageButton plus;
    private ImageButton minus;


    public ShoppingListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if(null != getActivity().getCurrentFocus()){
                    /**
                     * 点击空白位置 隐藏软键盘
                     */
                    InputMethodManager mInputMethodManager = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                    return mInputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                }
                return true;
            }
        });

        add = view.findViewById(R.id.ADD);
        plus = view.findViewById(R.id.ibn_add1);
        minus = view.findViewById(R.id.ibn_del1);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentText = etContent1.getText().toString();
                currentNum = etContent2.getText().toString();
                addContent(v,currentNum, currentText);
                etContent1.setText("");
                etContent2.setText("0");
            }
        });

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = Integer.parseInt(etContent2.getText().toString());
                temp += 1;
                etContent2.setText(String.valueOf(temp));
            }
        });

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = Integer.parseInt(etContent2.getText().toString());
                if (temp!=0) {
                    temp -= 1;
                }
                else {
                    temp = 0;
                }
                etContent2.setText(String.valueOf(temp));
            }
        });
        initCtrl();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shopping_list, container, false);
    }



    private void initCtrl()
    {
        llContentView = (LinearLayout) this.getView().findViewById(R.id.content_view);
        etContent1 = (EditText) this.getView().findViewById(R.id.et_content1);
        etContent2 = (EditText) this.getView().findViewById(R.id.et_content2);
        scroll = (ScrollView) this.getView().findViewById(R.id.scrollView3);

        listIBTNAdd = new LinkedList<ImageButton>();
        listCBFinish = new LinkedList<CheckBox>();
        listTVNumber = new LinkedList<TextView>();
        listIBTNDel = new LinkedList<ImageButton>();

        // “+”按钮（第一个）
/*        ImageButton ibtnAdd1 = (ImageButton) this.findViewById(R.id.ibn_add1);
        ibtnAdd1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 获取尺寸变化比例
                iETContentHeight = etContent1.getHeight();
                numberETHeight = etContent2.getHeight();
                fDimRatio = iETContentHeight / 80;

//                addContent(v);
            }
        });

        listIBTNAdd.add(ibtnAdd1);
        listIBTNDel.add(null);  // 第一组隐藏了“-”按钮，所以为null*/
    }

    private void addContent(final View v, String currentNum, String currentText) {

        if (v == null) {

            return;
        }

        if (iIndex >= 0) {
            iIndex = iIndex + 1;

            final int[] count = {0};
            // 开始添加控件

            // 1.创建外围LinearLayout控件
            LinearLayout layout = new LinearLayout( getActivity());
            LinearLayout.LayoutParams lLayoutlayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            // 设置margin
            lLayoutlayoutParams.setMargins(0, (int) (fDimRatio * 5), 0, 0);
            layout.setLayoutParams(lLayoutlayoutParams);
            // 设置属性
            layout.setBackgroundColor(Color.argb(255, 255, 255, 255));   // #FFA2CD5A
            layout.setPadding((int) (fDimRatio * 5), (int) (fDimRatio * 5),
                    (int) (fDimRatio * 5), (int) (fDimRatio * 5));
            layout.setOrientation(LinearLayout.VERTICAL);


            // 2.创建内部TextView控件
            final TextView tvContent = new TextView(getActivity());
            LinearLayout.LayoutParams tvParam = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tvContent.setLayoutParams(tvParam);
            tvContent.setBackgroundColor(Color.argb(255, 255, 255, 255));   // #FFFFFFFF
            tvContent.setGravity(Gravity.LEFT);
            tvContent.setPadding((int) (fDimRatio * 5), 0, 0, 0);
            tvContent.setTextSize(24);
            tvContent.setText(currentText);
            layout.addView(tvContent);

            // 3.创建checkbox & number外围控件RelativeLayout
            RelativeLayout rlBtn = new RelativeLayout(getActivity());
            RelativeLayout.LayoutParams rlParam = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            rlParam.setMargins(0, (int) (fDimRatio * 5), 0, 0);
            rlBtn.setPadding(0, (int) (fDimRatio * 5), 0, 0);
            rlBtn.setLayoutParams(rlParam);

            //创建checkbox控件
            CheckBox cb = new CheckBox(getActivity());
            RelativeLayout.LayoutParams cbParam = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            cbParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            cb.setLayoutParams(cbParam);
            cb.setText("Finished!");
            cb.setGravity(Gravity.CENTER_HORIZONTAL);
            cb.setId(checkIDIndex);
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    deleteContent(v);
                }
            });

            rlBtn.addView(cb);
            listCBFinish.add(cb);

            //创建number Textview
            final TextView tv2 = new TextView(getActivity());
            RelativeLayout.LayoutParams tvParam2 = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            tvParam2.addRule(RelativeLayout.LEFT_OF,editIDIndex);
            tv2.setLayoutParams(tvParam2);
            // 设置属性
            tv2.setBackgroundColor(Color.argb(255, 255, 255, 255));   // #FFFFFFFF
            tv2.setGravity(Gravity.CENTER_HORIZONTAL);
            tv2.setTextSize(14);
            tv2.setText("Amount: ");

            rlBtn.addView(tv2);

            //创建 添加数目
            final TextView et = new TextView(getActivity());
            RelativeLayout.LayoutParams etParam2 = new RelativeLayout.LayoutParams(
                    30, ViewGroup.LayoutParams.MATCH_PARENT);
            etParam2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            et.setLayoutParams(etParam2);
            et.setId(editIDIndex);
            et.setBackgroundColor(Color.argb(255, 255, 255, 255));   // #FFFFFFFF
            et.setGravity(Gravity.CENTER_HORIZONTAL);
            et.setTextSize(14);
            et.setText(currentNum);
            et.setInputType(InputType.TYPE_CLASS_NUMBER);

            rlBtn.addView(et);
            listTVNumber.add(et);

            // 4.创建“+”按钮
/*           ImageButton btnAdd = new ImageButton(MainActivity.this);
            RelativeLayout.LayoutParams btnAddParam = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            // 靠右放置
            btnAddParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            btnAdd.setLayoutParams(btnAddParam);
            // 设置属性
            btnAdd.setBackgroundResource(R.drawable.ic_add_18pt);
            btnAdd.setId(btnIDIndex);
            // 设置点击操作
            btnAdd.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    count[0] = count[0] +1;
                    et.setText(count[0]);
                }
            });
            // 将“+”按钮放到RelativeLayout里
            rlBtn.addView(btnAdd);
            listIBTNAdd.add(iIndex, btnAdd);

            // 5.创建“-”按钮
            ImageButton btnDelete = new ImageButton(MainActivity.this);
            btnDelete.setBackgroundResource(R.drawable.ic_remove_18pt);
            RelativeLayout.LayoutParams btnDeleteAddParam = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            btnDeleteAddParam.setMargins(0, 0, (int) (fDimRatio * 5), 0);
            // “-”按钮放在“+”按钮左侧
            btnDeleteAddParam.addRule(RelativeLayout.RIGHT_OF, checkIDIndex);
            btnDelete.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    deleteContent(v);
                }
            });
            // 将“-”按钮放到RelativeLayout里
            rlBtn.addView(btnDelete, btnDeleteAddParam);
            listIBTNDel.add(iIndex, btnDelete);*/

            // 6.将RelativeLayout放到LinearLayout里
            layout.addView(rlBtn);

            // 7.将layout同它内部的所有控件加到最外围的llContentView容器里
            llContentView.addView(layout, iIndex);

            btnIDIndex++;
            editIDIndex++;
            checkIDIndex++;
        }
    }

    /**
     * 删除一组控件
     * @param v 事件触发控件，其实就是触发删除事件对应的“-”按钮
     */
    private void deleteContent(View v) {
        if (v == null) {
            return;
        }

        // 判断第几个“-”按钮触发了事件
        int iIndex = 0;
        for (int i = 0; i < listCBFinish.size(); i++) {
            if (listCBFinish.get(i) == v) {
                iIndex = i;
                break;
            }
        }
        if (iIndex >= 1) {
//            listIBTNAdd.remove(iIndex);
            listCBFinish.remove(iIndex);
            listTVNumber.remove(iIndex);


            // 从外围llContentView容器里删除第iIndex控件
            llContentView.removeViewAt(iIndex);

            iIndex = iIndex-1;
        }
    }



}
