package cn.violin.home.auth.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoVo {

    @JsonProperty("id")
    private String id;

    @JsonProperty("account")
    private String account;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("baidu_name")
    private String baiduName;

    @JsonProperty("netdisk_name")
    private String netdiskName;
}
