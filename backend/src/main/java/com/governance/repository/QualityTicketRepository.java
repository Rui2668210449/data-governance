package com.governance.repository;

import com.governance.entity.QualityTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QualityTicketRepository extends JpaRepository<QualityTicket, Long> {
}
