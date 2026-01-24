package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.AboutUs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AboutUsRepository extends JpaRepository<AboutUs,Integer> {
}
