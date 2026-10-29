package com.fintech.transaction.client;

import com.fintech.transaction.dto.FraudCheckRequest;
import com.fintech.transaction.dto.FraudCheckResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "fraud-detection-service")
public interface FraudClient {
    @PostMapping("/api/fraud/internal/check")
    FraudCheckResponse check(@RequestBody FraudCheckRequest request, @RequestHeader("X-Internal-Key") String key);
}