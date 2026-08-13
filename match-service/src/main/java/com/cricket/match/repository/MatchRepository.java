package com.cricket.match.repository;

import com.cricket.match.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {
}
