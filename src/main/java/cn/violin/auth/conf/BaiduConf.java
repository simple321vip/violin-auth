package cn.violin.auth.conf;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class BaiduConf {

    @Value("${BaiDu.AppID}")
    private String appID;

    @Value("${BaiDu.AppKey}")
    private String appKey;

    @Value("${BaiDu.SecretKey}")
    private String secretKey;

    @Value("${BaiDu.RedirectUri}")
    private String redirectUri;

    @Value("${BaiDu.AuthorizeUri}")
    private String authorizeUri;

    @Value("${BaiDu.Scope}")
    private String scope;

    @Value("${BaiDu.QrCode}")
    private String qrCode;

    @Value("${BaiDu.AccessToken}")
    private String accessToken;
}
