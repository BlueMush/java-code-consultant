package com.example.point;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class PointServiceTest {

    @MockBean
    private PointRepository pointRepository;

    @Test
    void testCalculate() {
        PointService service = new PointService();
        assertEquals(100.0, service.calculateReward(1000.0, 0.1));
    }

    @Test
    void testNullElement() {
        assertThrows(NullPointerException.class,
                () -> new RewardBatch(List.of(null)).total());
    }
}
