package cn.violin.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_TENANT")
@Builder
public class Tenant {

    @Id
    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "account")
    private String account;

    @Column(name = "tel")
    private String tel;

    @Column(name = "authority")
    private int authority;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "storage_account")
    private String storageAccount;

}
