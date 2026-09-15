package com.hss01248.update_zealot;

import androidx.annotation.Keep;

import java.util.List;

/**
 * Zealot GET /api/apps/latest 响应。
 * 成功时多为：顶层 app/scheme + {@code releases: [ {...} ]}。
 */
@Keep
public class ZealotUpdateInfo {

    public String app_name;
    public String bundle_id;
    public int version;
    public Integer id;
    public String release_version;
    public String build_version;
    public String source;
    public String branch;
    public String git_commit;
    public String ci_url;
    public long size;
    public String icon_url;
    public String install_url;
    public String release_url;
    public String qrcode_url;
    public String text_changelog;
    public List<ChangelogItem> changelog;
    public String created_at;

    /** 部分文档形态：单个 release */
    public ZealotUpdateInfo release;

    /** 实测 6.2.x：releases 数组，取最新一条 */
    public List<ZealotUpdateInfo> releases;

    @Keep
    public static class ChangelogItem {
        public String message;
        public String author;
        public String email;
        public String date;
    }

    public ZealotUpdateInfo effective() {
        if (releases != null && !releases.isEmpty() && releases.get(0) != null) {
            return releases.get(0);
        }
        if (release != null) {
            return release;
        }
        return this;
    }

    public String resolveChangelog() {
        ZealotUpdateInfo e = effective();
        if (e.text_changelog != null && !e.text_changelog.trim().isEmpty()) {
            return e.text_changelog;
        }
        if (e.changelog != null && !e.changelog.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ChangelogItem item : e.changelog) {
                if (item == null || item.message == null) {
                    continue;
                }
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(item.message);
            }
            return sb.toString();
        }
        return "";
    }
}
