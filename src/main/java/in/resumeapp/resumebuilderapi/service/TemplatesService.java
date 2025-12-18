package in.resumeapp.resumebuilderapi.service;

import in.resumeapp.resumebuilderapi.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static in.resumeapp.resumebuilderapi.util.AppConstants.PREMIUM;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplatesService {

    private final AuthService authService;

    public Map<String, Object> getTemplates(Object principal) {

        //Step 1: Get the current profile.
        AuthResponse authResponse = authService.getProfile(principal);

        //Step 2: Get the available templates based on subscription.
        List<String> availableTemplates;

        Boolean isPremium = PREMIUM.equalsIgnoreCase(authResponse.getSubscriptionPlan());

        if(isPremium) {
            availableTemplates = List.of("01", "02", "03");
        }else {
            availableTemplates = List.of("01");
        }

        //Step 3: Add the data into map.
        Map<String, Object> restrictions = new HashMap<>();
        restrictions.put("availableTemplates", availableTemplates);
        restrictions.put("allTemplates", List.of("01", "02", "03"));
        restrictions.put("subscriptionPlan", authResponse.getSubscriptionPlan());
        restrictions.put("isPremium", isPremium);

        //Step 4: Return the result.
        return restrictions;
    }
}
