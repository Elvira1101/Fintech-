package com.fintech.fraud.controller;

import com.fintech.fraud.dto.FraudAlertResponse;
import com.fintech.fraud.dto.FraudCheckRequest;
import com.fintech.fraud.dto.FraudCheckResponse;
import com.fintech.fraud.service.FraudService;
import com.fintech.common.security.InternalRequestValidator;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/fraud")
public class FraudController {
    private final FraudService service;
    private final InternalRequestValidator internalValidator;

    public FraudController(FraudService service, InternalRequestValidator internalValidator) {
        this.service = service;
        this.internalValidator = internalValidator;
    }

    @PostMapping("/internal/check")
    public FraudCheckResponse check(@Valid @RequestBody FraudCheckRequest request,
                                    @RequestHeader("X-Internal-Key") String key) {
        internalValidator.requireValid(key);
        return service.evaluate(request);
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasRole('RISK_ANALYST')")
    public List<FraudAlertResponse> alerts() {
        return service.alerts();
    }
}