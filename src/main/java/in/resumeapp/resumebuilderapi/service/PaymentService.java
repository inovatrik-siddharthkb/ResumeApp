package in.resumeapp.resumebuilderapi.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import in.resumeapp.resumebuilderapi.document.Payment;
import in.resumeapp.resumebuilderapi.document.User;
import in.resumeapp.resumebuilderapi.dto.AuthResponse;
import in.resumeapp.resumebuilderapi.repository.PaymentRepository;
import in.resumeapp.resumebuilderapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static in.resumeapp.resumebuilderapi.util.AppConstants.PREMIUM;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AuthService authService;
    private final UserRepository userRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    @Value("${razorpay.key.secrets}")
    private String razorpayKeySecret;

    public Payment createOrder(Object principal, String planType) throws RazorpayException {

        //Initial
        AuthResponse authResponse = authService.getProfile(principal);

        //Step 1: Initialize the razorpay client.
        RazorpayClient razorPayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        //Step 2: Prepare the JSON object to pass the razorpay.
        int amount = 99900; //Amount in paisa.
        String currency = "INR";
        String receipt = PREMIUM+"_"+ UUID.randomUUID().toString().substring(0, 8);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount);
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", receipt);

        //Step 3: Call the razorpay API to create order.
        Order razorpayOrder = razorPayClient.orders.create(orderRequest);

        //Step 4: Save the order details into database.
        Payment newPayment = Payment.builder()
                .userId(authResponse.getId())
                .razorpayOrderId(razorpayOrder.get("id"))
                .amount(amount)
                .currency(currency)
                .planType(planType)
                .status("created")
                .receipt(receipt)
                .build();

        //Step 5: Return the result.
        return paymentRepository.save(newPayment);
    }

    public boolean verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) throws RazorpayException {

        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", razorpayOrderId);
            attributes.put("razorpay_payment_id", razorpayPaymentId);
            attributes.put("razorpay_signature", razorpaySignature);

            boolean isValidSignature = Utils.verifyPaymentSignature(attributes, razorpayKeySecret);

            if(isValidSignature) {
                //Update payment status.
                Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                        .orElseThrow(() -> new RuntimeException("Payment not found."));
                payment.setRazorpayPaymentId(razorpayPaymentId);
                payment.setRazorpaySignature(razorpaySignature);
                payment.setStatus("paid");
                paymentRepository.save(payment);

                //Upgrade the user subscription.
                upgradePaymentSubscription(payment.getUserId(), payment.getPlanType());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error verifying the payment:", e);
            return false;
        }
    }

    private void upgradePaymentSubscription(String userId, String planType) {

        User existingUser = userRepository.findById(userId)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found."));
        existingUser.setSubscriptionPlan(planType);
        userRepository.save(existingUser);
        log.info("User {} upgraded to {} plan.", userId, planType);
    }

    public List<Payment> getUserPayments(Object principal) {
        //Step 1: Get the current profile.
        AuthResponse authResponse = authService.getProfile(principal);

        //Step 2: Call the repository finder method.
        return  paymentRepository.findByUserIdOrderByCreatedAtDesc(authResponse.getId());
    }

    public Payment getPaymentDetails(String orderId) {
        //Step 1: Call the repository finder method.
        return paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found."));
    }
}
