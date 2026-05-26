package net.guides.springboot2.crud.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.guides.springboot2.crud.model.Worker;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, Long> {

    Optional<Worker> findByPhone(String phone);

    boolean existsByPhone(String phone);
}
