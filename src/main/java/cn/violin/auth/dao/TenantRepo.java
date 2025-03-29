package cn.violin.auth.dao;

import cn.violin.auth.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepo extends JpaRepository<Tenant, String> {
}
