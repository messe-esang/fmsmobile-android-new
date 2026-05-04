package net.e_sang.fmsmobile.data;

/**
 * Created by BaekHyunJae on 2016-12-24.
 */

public class PermissionSet {
    public String mPermissionGroup = "";
    public String mPermission = "";

    public PermissionSet(String permissionGroup, String permission) {
        mPermissionGroup = permissionGroup;
        mPermission = permission;
    }
}
