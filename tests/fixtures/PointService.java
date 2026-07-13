package com.example.point;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PointService {

    @Autowired
    private PointRepository pointRepository;

    public double calculateReward(double price, double rate) {
        double reward = price * rate;
        if (reward > 10000) {
            reward = 10000;
        }
        return reward;
    }

    public java.util.Optional<String> findName(java.util.Optional<Long> id) {
        if (id.isPresent()) {
            return java.util.Optional.of(pointRepository.findName(id.get()));
        }
        return null;
    }
}
