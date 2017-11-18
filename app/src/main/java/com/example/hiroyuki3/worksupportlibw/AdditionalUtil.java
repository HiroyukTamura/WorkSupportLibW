package com.example.hiroyuki3.worksupportlibw;

import android.support.annotation.NonNull;

import com.cks.hiroyuki2.worksupprotlib.Entity.User;

import java.util.List;

/**
 * Created by hiroyuki2 on 2017/11/07.
 */

public class AdditionalUtil {
    public static final String ADD_NEW_TAG = "ADD_NEW_TAG";

    //fragCode
    public static final int CODE_EDIT_FRAG = 10;
    public static final int CODE_RECORD_FRAG = 11;
    public static final int CODE_BLANK_FRAG = 12;

    //SocialListRVAdapter
    public static final int CODE_ADD_GROUP_FRAG = 0;
    public static final int CODE_SOCIAL_FRAG = 1;

    public final static String DIALOG_TAG_ITEM_VERT = "DIALOG_TAG_ITEM_VERT";//rvのitemでvertをclickしたらdialogを出す、その時のやつ。
    public final static int DIALOG_CODE_ITEM_VERT = 1144;
    public final static String DIALOG_TAG_DOC_VERT = "DIALOG_TAG_DOC_VERT";
    public final static int DIALOG_CODE_DOC_VERT = 1146;
    public final static String DIALOG_TAG_DATA_VERT = "DIALOG_TAG_DOC_DATA_VERT";
    public final static int DIALOG_CODE_DATA_VERT = 1147;

    /**
     * @return 例外時Integer.MAX_VALUE
     * todo libに移植できる
     */
    public static  int getPosFromUid(@NonNull List<User> userList, @NonNull String uid){
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUserUid().equals(uid)){
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }
}
