package com.leroy.pulsecheckapi.repository;

import com.leroy.pulsecheckapi.model.Monitor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonitorRepository extends JpaRepository<Monitor, String> {
}
