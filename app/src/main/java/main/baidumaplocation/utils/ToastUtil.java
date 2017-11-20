package main.baidumaplocation.utils;

import android.view.Gravity;
import android.widget.Toast;

import main.baidumaplocation.application.AppContext;

public class ToastUtil {

    private static Toast toast = null;
    private static int LENGTH_SHORT = Toast.LENGTH_SHORT;

   /* public static void TextToast(String msg) {
        toast = Toast.makeText(AppContext.getContext(), msg, LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }*/
    /**
     * 中间Toast
     *
     * @param msg
     */
    public static void TextToast(String msg) {
        if (toast != null) {
            toast.cancel();
        }
        // 创建一个Toast提示消息
        toast = Toast.makeText(AppContext.getContext(), msg, LENGTH_SHORT);
        // 设置Toast提示消息在屏幕上的位置
        toast.setGravity(Gravity.CENTER, 0, 0);


        /*//获取自定义视图
        View view = LayoutInflater.from(AppContext.getContext()).inflate(R.layout.toast_view, null);
        TextView tvMessage = (TextView) view.findViewById(R.id.tv_message_toast);
        //设置文本
        tvMessage.setText(msg);
        //设置视图
        toast.setView(view);*/


        // 显示消息
        toast.show();
    }

}