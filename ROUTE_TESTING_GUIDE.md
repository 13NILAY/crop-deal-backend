# CropDeal API Route Testing Guide

This runbook tests the local services through the API Gateway wherever a gateway route exists. It follows the business dependencies so IDs created earlier can be reused later. Use Postman or `curl.exe`; the methods, paths, headers, and JSON examples below are the same in either tool.

## 1. Start the application

Start MySQL and RabbitMQ first, then start services in this order:

1. Config Server: `http://localhost:8888`
2. Eureka Server: `http://localhost:8761`
3. Authentication Service: `http://localhost:8081`
4. Crop Service: `http://localhost:8082`
5. Profile Service: `http://localhost:8085`
6. Order Service: `http://localhost:8087`
7. Payment Service: `http://localhost:8088`
8. Notification Service: `http://localhost:8089`
9. Subscription Service: `http://localhost:8090`
10. API Gateway: `http://localhost:8080`

Use the Gateway as the base URL for normal client calls. It uses Eureka to locate services, so wait for registration before testing Gateway paths. Swagger UI is at `http://localhost:8080/swagger-ui.html`. OpenAPI specs are `/authentication/v3/api-docs`, `/profile/v3/api-docs`, `/crop/v3/api-docs`, `/order/v3/api-docs`, `/payment/v3/api-docs`, `/notification/v3/api-docs`, and `/subscription/v3/api-docs` on port 8080.

Check health directly before continuing:

| Service | Health URL |
| --- | --- |
| Gateway | `http://localhost:8080/actuator/health` |
| Authentication | `http://localhost:8081/actuator/health` |
| Crop | `http://localhost:8082/actuator/health` |
| Profile | `http://localhost:8085/actuator/health` |
| Order | `http://localhost:8087/actuator/health` |
| Payment | `http://localhost:8088/actuator/health` |
| Notification | `http://localhost:8089/actuator/health` |
| Subscription | `http://localhost:8090/actuator/health` |
| Config Server | `http://localhost:8888/actuator/health` |
| Eureka | `http://localhost:8761/actuator/health` |

Expected: HTTP `200` with `{"status":"UP"}`. Also open `http://localhost:8761` and confirm the services are registered.

## 2. Prepare test users and headers

Use fresh email addresses for repeatable runs. Create separate Farmer and Dealer users. An Admin account must already have the `ADMIN` role; do not promote a production account for testing. Login returns a JWT; save it in Postman and send `Authorization: Bearer <token>` on protected requests.

Register and login examples:

```http
POST http://localhost:8080/auth/register
Content-Type: application/json

{"email":"farmer.test@example.com","password":"TestPass123","role":"FARMER"}
```

```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{"email":"farmer.test@example.com","password":"TestPass123"}
```

Repeat for a Dealer with role `DEALER`. To add a role, call `POST /auth/roles?role=FARMER` with the user's current JWT, then login again to get a JWT containing the updated roles. Separate users make ownership and role checks easier to verify.

Substitute response IDs for `<farmerId>`, `<dealerId>`, `<cropId>`, `<orderId>`, `<paymentId>`, `<bankAccountId>`, `<payoutId>`, and `<notificationId>` in later requests.

## 3. Run the end-to-end flow

Run calls in this order. Use the Farmer JWT for farmer-owned data, Dealer JWT for dealer-owned data, and Admin JWT for admin routes.

### A. Farmer profile

1. Create: `POST /farmers` with Farmer JWT and body `{"fullName":"Test Farmer","phoneNumber":"+15551234567","address":"10 Test Road","location":"Springfield"}`. Expect `201`.
2. Confirm profile: `GET /farmers/me`; save its `id` as `<farmerId>`.
3. Read: `GET /farmers/<farmerId>`.
4. List: `GET /farmers`.
5. Update: `PUT /farmers/<farmerId>` with the same fields and one changed value. Expect `200`.

### B. Dealer profile

6. Create: `POST /dealers` with Dealer JWT and body `{"businessName":"Test Produce","contactName":"Test Dealer","phoneNumber":"+15557654321","address":"20 Market Road","location":"Springfield"}`. Expect `201`; save `id` as `<dealerId>`.
7. Read: `GET /dealers/<dealerId>`.
8. Update: `PUT /dealers/<dealerId>` with the same body and one changed value.

### C. Crop listing and search

9. Create: `POST /crops` with Farmer JWT and body `{"farmerId":<farmerId>,"cropName":"Test Corn","cropType":"GRAIN","quantity":100,"price":25.50,"location":"Springfield"}`. Expect `201`; save `id` as `<cropId>`.
10. Public list: `GET /crops` without a JWT.
11. Detail: `GET /crops/<cropId>` with a valid Farmer or Dealer JWT. The Gateway does not mark this detail route public.
12. Name search: `GET /crops/search?name=Corn`.
13. Type filter: `GET /crops/filter/type?type=GRAIN`.
14. Location search: `GET /crops/search/location?location=Springfield`.
15. Pagination: `GET /crops/page?page=0&size=10&sortBy=id&direction=asc`.
16. Update: `PUT /crops/<cropId>` with Farmer JWT and the create body (or changed values).
17. Reservation: `POST /crops/<cropId>/reserve?farmerId=<farmerId>&quantity=1` with authentication. Use a small quantity so the later order still has inventory.

### D. Subscription

18. Subscribe: `POST /subscriptions/farmers/<farmerId>` with Dealer JWT. Repeating should be idempotent.
19. Read Dealer subscriptions: `GET /subscriptions/mine` with Dealer JWT.
20. Internal lookup: `GET /subscriptions/farmers/<farmerId>/dealers` with `X-Internal-Token: <SUBSCRIPTION_INTERNAL_TOKEN>`. Use the direct URL `http://localhost:8090` if the Gateway requires a JWT for this trusted service call.

### E. Order and order reads

21. Create: `POST /orders` with Dealer JWT and body `{"cropId":<cropId>,"farmerId":<farmerId>,"quantity":2}`. Expect `201`; save `id` as `<orderId>`. This reserves crop quantity and publishes an event. Allow time for RabbitMQ consumers.
22. Read: `GET /orders/<orderId>`.
23. Dealer history: `GET /orders/dealer/me` with Dealer JWT.
24. Farmer history: `GET /orders/farmer/<farmerId>` with authentication.
25. Admin order list/detail (Admin JWT): `GET /admin/orders`, then `GET /admin/orders/<orderId>`.
26. Do not manually change status during the main flow. Payment events should change the order to `CONFIRMED` or `CANCELLED`. The manual route is `PUT /orders/<orderId>/status?status=<OrderStatus>`.

### F. Payment and bank accounts

27. Automatic payment: after the order event is consumed, call `GET /payments/order/<orderId>` with Dealer JWT. If no payment appears, check RabbitMQ, Payment Service logs, Razorpay test credentials, and the event consumer.
28. Optional manual payment: `POST /payments` with Dealer JWT and body `{"orderId":<orderId>,"amount":51.00}`. Use the actual order total. Save payment `id` as `<paymentId>`.
29. Read payment: `GET /payments/<paymentId>` and `GET /payments/order/<orderId>`.
30. Create a test bank account: `POST /bank-accounts` with Farmer JWT and body `{"accountHolderName":"Test Farmer","accountNumber":"123456789012","ifscCode":"ABCD0123456","bankName":"Test Bank","defaultAccount":true,"razorpayLinkedAccountId":""}`. Expect `201`; save `id` as `<bankAccountId>`. Use synthetic details only; response values should be masked.
31. List and update: `GET /bank-accounts`, then `PUT /bank-accounts/<bankAccountId>` with the same fields and a changed value.
32. Checkout verification: `POST /payments/verify` with Dealer JWT and body `{"razorpayOrderId":"<real-test-order-id>","razorpayPaymentId":"<real-test-payment-id>","razorpaySignature":"<real-test-signature>"}`. Requires a real Razorpay test Checkout result; fabricated signatures should fail.
33. Webhook: `POST /payments/webhook` with a genuine Razorpay test webhook payload and `X-Razorpay-Signature: <valid-signature>`. Arbitrary manual JSON is not a valid test. Valid captured/failed events update payment state; successful payment creates invoice and receipt documents.
34. After a successful payment, download `GET /payments/<paymentId>/invoice` and `GET /payments/<paymentId>/receipt` with the owning Dealer JWT. Expect `application/pdf`; verify another user's JWT is rejected.
35. Farmer payout history: `GET /payouts/farmer/me` with Farmer JWT.
36. Admin payout list/detail: `GET /payouts` and `GET /payouts/<payoutId>` with Admin JWT.
37. Create payout: `POST /payouts` with Admin JWT and body `{"orderId":<orderId>,"farmerId":<farmerId>,"farmerEmail":"farmer.test@example.com","bankAccountId":<bankAccountId>,"grossAmount":51.00,"commissionAmount":5.10}`. Save `id` as `<payoutId>`.
38. Update payout status: `PUT /payouts/<payoutId>/status?status=<PayoutStatus>` with Admin JWT.
39. Execute payout: `POST /payouts/<payoutId>/execute` with Admin JWT. Requires a verified account with a Razorpay test linked-account ID; skip if that setup is unavailable.
40. Admin payment search/detail: `GET /admin/payments`, `GET /admin/payments?status=<PaymentStatus>`, `GET /admin/payments?orderId=<orderId>`, and `GET /admin/payments/<paymentId>` with Admin JWT.

### G. Notifications and verification

41. Read notifications: `GET /notifications` with the relevant Farmer/Dealer JWT. Order/payment events should create notifications if RabbitMQ consumers processed them.
42. Mark read: `PUT /notifications/<notificationId>/read` with that same user's JWT. Expect `204`; another user's JWT should not be able to mark it read.
43. Admin queues: `GET /admin/verifications/farmers?status=PENDING` and `GET /admin/verifications/dealers?status=PENDING` with Admin JWT.
44. Update verification: `PATCH /admin/verifications/farmers/<farmerId>` or `/admin/verifications/dealers/<dealerId>` with Admin JWT and body `{"status":"VERIFIED"}`. Confirm by repeating the list with `status=VERIFIED`.

### H. Admin routes

Use an Admin JWT for these routes:

| Service | Route(s) |
| --- | --- |
| Gateway | `GET /admin/dashboard`, `GET /admin/health` |
| Authentication | `GET /admin/users`, `GET /admin/users?search=<text>&role=<role>`, `GET /admin/users/<id>`, `PATCH /admin/users/<id>/status` with `{"active":false}` or `{"active":true}`, `GET /admin/activity` |
| Crop | `GET /admin/crops`, `GET /admin/crops/<id>`, `PATCH /admin/crops/<id>/status` with `{"status":"INACTIVE"}` or `{"status":"ACTIVE"}` |
| Order | `GET /admin/orders`, `GET /admin/orders/<id>` |
| Payment | `GET /admin/payments`, `GET /admin/payments/<id>` |
| Payout | `GET /payouts`, `GET /payouts/<id>`, `POST /payouts`, `PUT /payouts/<id>/status`, `POST /payouts/<id>/execute` |
| Subscription | `GET /admin/subscriptions`, `GET /admin/subscriptions/<id>` |
| Profile | `GET /admin/verifications/farmers`, `GET /admin/verifications/dealers`, `PATCH /admin/verifications/farmers/<id>`, `PATCH /admin/verifications/dealers/<id>` |

Paged searches accept query parameters; use `page=0&size=20` for a small response. Admin writes affect test data, so restore statuses after testing.

## 4. Cleanup (run last)

1. `DELETE /subscriptions/farmers/<farmerId>` with Dealer JWT. Expect `204`.
2. `DELETE /bank-accounts/<bankAccountId>` with Farmer JWT. Expect `204`.
3. `DELETE /crops/<cropId>` with Farmer JWT. Expect `204` if order/payment constraints allow it.
4. `DELETE /farmers/<farmerId>` with Farmer JWT and `DELETE /dealers/<dealerId>` with Dealer JWT. Expect `204`.

If a business record prevents deletion, leave it in the test database rather than deleting rows manually. Use fresh emails and record created IDs for the next run.

## 5. Route inventory and access notes

Unless noted otherwise, prepend `http://localhost:8080` and send the role-appropriate Bearer JWT. Public crop reads are the exception.

| Service | Method and path | Access / notes |
| --- | --- | --- |
| Authentication | `POST /auth/register`, `POST /auth/login` | Public |
| Authentication | `POST /auth/roles?role=<role>` | Authenticated |
| Authentication | `GET /auth/oauth2-success?token=...&email=...&role=...` | OAuth2 callback helper; use only as part of configured OAuth2 login |
| Authentication | `GET /admin/users`, `GET /admin/users/<id>`, `PATCH /admin/users/<id>/status` | Admin |
| Authentication | `GET /admin/activity` | Admin |
| Authentication | `GET /internal/admin/stats`, `POST /internal/admin/activity` | Admin; direct `http://localhost:8081`, not forwarded by Gateway |
| Profile | `POST /farmers`, `GET /farmers`, `GET /farmers/me`, `GET /farmers/<id>`, `PUT /farmers/<id>`, `DELETE /farmers/<id>` | Farmer for create/me/mutations; authenticated for list/detail |
| Profile | `POST /dealers`, `GET /dealers/<id>`, `PUT /dealers/<id>`, `DELETE /dealers/<id>` | Dealer for create/mutations; authenticated for detail |
| Profile | `GET /dealers/emails` | Direct `http://localhost:8085`; requires `X-Internal-Token`. Through Gateway, also send a JWT because `/dealers/**` is secured there. |
| Profile | `GET /admin/verifications/farmers`, `GET /admin/verifications/dealers`, `PATCH /admin/verifications/farmers/<id>`, `PATCH /admin/verifications/dealers/<id>` | Admin |
| Crop | `POST /crops`, `PUT /crops/<id>`, `DELETE /crops/<id>` | Owning Farmer JWT |
| Crop | `GET /crops`, `GET /crops/search?name=...`, `GET /crops/filter/type?type=...`, `GET /crops/search/location?location=...`, `GET /crops/page` | Public through Gateway |
| Crop | `GET /crops/<id>` | Gateway requires a valid JWT; Crop Service itself allows GET requests |
| Crop | `POST /crops/<id>/reserve?farmerId=...&quantity=...` | Authenticated; also called by Order Service |
| Crop | `GET /admin/crops`, `GET /admin/crops/<id>`, `PATCH /admin/crops/<id>/status` | Admin |
| Order | `POST /orders`, `GET /orders/<id>`, `GET /orders/dealer/me`, `GET /orders/farmer/<farmerId>`, `PUT /orders/<id>/status?status=...` | Authenticated; create requires Dealer role |
| Order | `GET /admin/orders`, `GET /admin/orders/<id>` | Admin |
| Payment | `POST /payments`, `POST /payments/verify`, `POST /payments/webhook`, `GET /payments/<id>`, `GET /payments/order/<orderId>`, `PUT /payments/<id>/status?status=...`, `GET /payments/<paymentId>/invoice`, `GET /payments/<paymentId>/receipt` | Authenticated except signed Razorpay webhook; documents are owner-only |
| Payment | `GET /bank-accounts`, `POST /bank-accounts`, `PUT /bank-accounts/<id>`, `DELETE /bank-accounts/<id>` | Authenticated owner |
| Payment | `GET /payouts`, `GET /payouts/<id>`, `POST /payouts`, `PUT /payouts/<id>/status?status=...`, `POST /payouts/<id>/execute` | Admin |
| Payment | `GET /payouts/farmer/me` | Farmer |
| Payment | `GET /admin/payments`, `GET /admin/payments/<id>` | Admin |
| Notification | `GET /notifications`, `PUT /notifications/<id>/read` | Authenticated owner |
| Subscription | `POST /subscriptions/farmers/<farmerId>`, `DELETE /subscriptions/farmers/<farmerId>`, `GET /subscriptions/mine` | Dealer |
| Subscription | `GET /subscriptions/farmers/<farmerId>/dealers` | Internal token; direct `http://localhost:8090` if needed |
| Subscription | `GET /admin/subscriptions`, `GET /admin/subscriptions/<id>` | Admin |
| Gateway | `GET /admin/dashboard`, `GET /admin/health` | Admin |

Admin statistics are `GET /internal/admin/stats` in Authentication, Crop, Order, Payment, and Subscription. They are not forwarded by Gateway; call the direct service ports `8081`, `8082`, `8087`, `8088`, and `8090` with an Admin JWT. `POST /internal/admin/activity` is also direct-only on port `8081`.

Known route/documentation mismatch: Profile Service's `SERVICE_GUIDE.md` lists `GET /dealers`, but the controller has no such mapping. Do not count it as a working route. The available dealer-email lookup is `GET /dealers/emails` and requires the internal token.

## 6. Success criteria

- Reads and updates normally return HTTP `200`; creates normally return `201`; successful deletes and notification-read operations return `204`.
- Protected routes reject missing/invalid JWTs; Admin/role-protected routes reject a valid token with the wrong role.
- Ownership checks reject unauthorized access to another user's records.
- Crop quantity decreases after reservation/order creation; the order event creates a payment when Payment Service and RabbitMQ are healthy.
- Payment success/failure events update the order, and notifications appear for the relevant user.
- Razorpay verification/webhook and payout execution require valid provider credentials and signed test data; a fabricated success payload is not a valid integration test.

Record each request, response status/body, and created ID as you go. If a route fails, capture the service log and request time before moving on; subsequent steps depend on earlier records.