package com.raoudate.GestionDeTri.repository;

import com.raoudate.GestionDeTri.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
}
