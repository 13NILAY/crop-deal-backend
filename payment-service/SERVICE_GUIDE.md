# Payment Service Guide

## Purpose
Payment Service owns payments, user bank accounts, payouts, commissions, Razorpay integration, and payment events.

## Runtime
- Port: `8088`
- Database: `cropdeal_payment`
- Communication: Razorpay, RabbitMQ, JWT

## Payment APIs
- `POST /payments`: create a Razorpay order
- `POST /payments/verify`: verify Checkout signature
- `POST /payments/webhook`: receive signed Razorpay events
- `GET /payments/{id}`: read payment
- `GET /payments/order/{orderId}`: payment history
- `GET /payments/{paymentId}/invoice`: download the generated invoice PDF
- `GET /payments/{paymentId}/receipt`: download the generated receipt PDF

## Bank-account APIs
- `GET /bank-accounts`
- `POST /bank-accounts`
- `PUT /bank-accounts/{id}`
- `DELETE /bank-accounts/{id}`

Bank accounts are linked to the authenticated user email. Account number and IFSC are encrypted at rest and masked in responses. One user can reuse one account as Farmer and Dealer or maintain multiple accounts.

## Payout APIs
- `POST /payouts`: create a payout record, Admin only
- `POST /payouts/{id}/execute`: execute Razorpay Transfer, Admin only
- `PUT /payouts/{id}/status`: update payout status, Admin only
- `GET /payouts/farmer/me`: farmer payout history

Payout execution requires a verified bank account with a Razorpay linked-account ID. The platform commission is deducted before calculating the farmer amount.

## Event communication
- Consumes `OrderCreatedEvent` from `cropdeal.orders`.
- Publishes payment status events to `cropdeal.payments`.
- The order-event consumer is duplicate-safe by `orderId`; rejected order events are routed to `payment-service.order-events.dlq` through `cropdeal.dlx`.
- Razorpay webhook/signature verification is required before trusted success status.
- Only `payment.captured` and `payment.failed` webhook events change payment state; unknown events are ignored and a successful payment cannot be downgraded.
- Successful verification or a captured Razorpay webhook creates one invoice and one receipt in `payment_documents`.
- Document creation is idempotent for repeated payment events; downloads are restricted to the payment owner.
- When `PAYMENT_EMAIL_ENABLED=true`, successful payments email both PDFs using the configured SMTP server. Delivery failures are logged and do not undo a successful payment.
