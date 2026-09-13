package open.dolphin.delegater;

import open.dolphin.infomodel.UserModel;
import open.dolphin.service.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * UserDelegeter.
 *
 * @author pns
 */
public class UserDelegater extends BusinessDelegater<UserService> {

    /**
     * ユーザを検索して返す.
     *
     * @param uid facilityId:username 型式の userId
     * @return UserModel
     */
    public UserModel getUser(String uid) {
        return getService().getUser(uid);
    }

    /**
     * 施設内の全ユーザを検索して返す。
     *
     * @return 施設内ユーザリスト
     */
    public List<UserModel> getAllUser() {
        return  new ArrayList<>(getService().getAllUser());
    }

    /**
     * ユーザを追加する.
     *
     * @param userModel 追加するユーザモデル
     * @return 追加件数
     */
    public int putUser(UserModel userModel) {
        return getService().addUser(userModel);
    }

    /**
     * ユーザ情報を更新する。
     *
     * @param userModel 更新するユーザモデル
     * @return 更新件数
     */
    public int updateUser(UserModel userModel) {
        return getService().updateUser(userModel);
    }

    /**
     * ユーザを削除する.
     *
     * @param uid 削除するユーザのId
     * @return 削除件数
     */
    public int removeUser(String uid) {
        return getService().removeUser(uid);
    }

    /**
     * 施設情報を更新する。
     *
     * @param user 更新するユーザモデル
     * @return 更新件数
     */
    public int updateFacility(UserModel user) {
        return getService().updateFacility(user);
    }
}
