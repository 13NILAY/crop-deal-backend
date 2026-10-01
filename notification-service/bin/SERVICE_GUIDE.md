# Notification Service Guide

## Purpose
Notification Service consumes business events, stores user-visible notifications, and optionally sends email.

## Runtime
- Port: `8089`
- Database: `cropdeal_notification`
- Messaging: RabbitMQ
- Email: optional SMTP

## APIs
- `GET /notifications`: list notifications for the authenticated user
- `PUT /notifications/{id}/read`: mark the user's notification as read

## Event flow
1. Order Service publishes `OrderCreatedEvent`.
2. Payment Service publishes payment status events.
3. RabbitMQ routes events to durable Notification queues.
4. Notification Service extracts the recipient email, stores the event message, and marks it unread.
5. If email is enabled, SMTP sends an email to the recipient.

## Configuration
Set `NOTIFICATION_EMAIL_ENABLED=true` and configure `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, and `NOTIFICATION_EMAIL_FROM` to send email. With email disabled, notifications are still stored in the database.

## Ownership
This service owns notification records only. It does not own users, payments, orders, or email provider credentials beyond runtime configuration.
