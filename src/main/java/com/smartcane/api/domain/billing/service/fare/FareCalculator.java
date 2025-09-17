package com.smartcane.api.domain.billing.service.fare;

import com.smartcane.api.domain.billing.dto.fare.FareBreakdown;
import com.smartcane.api.domain.billing.dto.fare.RideContext;

public interface FareCalculator {
    FareBreakdown calculate(RideContext ride);
}
