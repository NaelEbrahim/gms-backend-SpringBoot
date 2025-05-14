package com.graduation.GMS.Repositories;

import com.graduation.GMS.Models.FAQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FAQRepository extends JpaRepository<FAQ, Integer> {
}
